/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.execution

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.activation.{MediaType, MediaTypes}
import org.beangle.commons.io.Serializer
import org.beangle.commons.lang.Charsets
import org.beangle.commons.lang.annotation.description
import org.beangle.template.api.DynaProfile
import org.beangle.web.servlet.intercept.Interceptor
import org.beangle.web.servlet.resource.PathResolver
import org.beangle.webmvc.config.{ActionMapping, RouteMapping}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.util.CacheControl
import org.beangle.webmvc.view.*

import java.io.ByteArrayOutputStream

/**
 * 缺省的调用处理器：调用 Action，再按返回值渲染视图或写出响应体。
 *
 * 非 View 结果的写出策略：
 * - Array[Byte]：视为已编码的二进制，直接写出，不查 Serializer，也不附加 charset
 * - 其它类型：按 Accept 查找 Serializer；找不到则 toString 后按 UTF-8 写出
 * - cacheable：本地响应缓存 + Cache-Control s-maxage（秒数见 RouteMapping.maxAge）
 */
@description("缺省的调用处理器")
class DefaultMappingHandler(val mapping: RouteMapping, val invoker: Invoker,
                            viewManager: ViewManager,
                            responseCache: ResponseCache) extends MappingHandler {

  override def handle(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val action = mapping.action
    // 可缓存映射优先读本地响应缓存
    if (mapping.cacheable) {
      responseCache.get(request) match {
        case Some(cr) =>
          writeCachedToResponse(response, cr, mapping.maxAge)
          return
        case None =>
      }
    }
    val interceptors = action.profile.interceptors
    val context = ActionContext.current
    val lastInterceptorIndex = preHandle(interceptors, context, request, response)
    try {
      if (lastInterceptorIndex == interceptors.length - 1) {
        var result: Any = null
        try {
          result = invoker.invoke()
        } catch {
          case e: ResultException => result = StatusView(e.code, e.result)
          case e => throw e
        }
        val flash = context.getFlash(false)
        if (null != flash) flash.writeNextToCookie()

        var view: View = null
        result match {
          case v: View =>
            // 按出现频率从高到低处理常见 View
            v match {
              case PathView(path) =>
                val viewName = if (null == path) mapping.defaultView else path
                view =
                  if (DynaProfile.get.nonEmpty) {
                    resolveView(viewName, action)
                  } else {
                    action.views.get(viewName) match
                      case Some(v) => v
                      case None => resolveView(viewName, action)
                  }
              case dv: StatusView => response.setStatus(dv.status); result = dv.body
              case sv: StreamView => StreamViewRender.render(sv, context); result = null
              case rv: RawView => RawViewRender.render(rv, context); result = null
              case _ => view = v
            }
          case _ =>
        }

        if (null != view) {
          viewManager.getRender(view.getClass) match {
            case Some(render) => render.render(view, context)
            case None => throw new RuntimeException(s"Cannot find render for ${view.getClass}")
          }
        } else if (null != result) {
          // 统一编码为 (Content-Type, bytes)，便于缓存与写出共用
          val (contentType, bytes) = result match {
            case data: Array[Byte] =>
              // 二进制已就绪：跳过 Serializer；Content-Type 取 Accept 首项，缺省 octet-stream
              val ct = context.acceptTypes.headOption.map(_.toString).getOrElse(MediaTypes.stream.toString)
              (ct, data)
            case other => encodeResult(other, request, context)
          }
          if (mapping.cacheable) {
            responseCache.put(request, CacheResult.of(response, contentType, bytes))
            writeToResponse(response, contentType, bytes, mapping.maxAge)
          } else {
            writeToResponse(response, contentType, bytes, 0)
          }
        }
      }
    } finally {
      //FIXME process exception
      postHandle(interceptors, context, lastInterceptorIndex, request, response)
    }
  }

  private def resolveView(viewName: String, action: ActionMapping): View = {
    val profile = action.profile
    viewManager.getResolver(profile.viewType) match {
      case Some(resolver) =>
        var i = 0
        val candidates = PathResolver.resolve(viewName)
        var newView: View = null
        while (i < candidates.length && null == newView) {
          newView = resolver.resolve(candidates(i), mapping)
          i += 1
        }
        require(null != newView, s"Cannot find view[$viewName] for ${action.clazz.getName}")
        newView
      case None =>
        throw new RuntimeException(s"Cannot find view of type [${profile.viewType}]'s resolver")
    }
  }

  /** 按 Accept 选择 Serializer 编码；无可用 Serializer 时回退为 UTF-8 文本。 */
  private def encodeResult(result: Any, request: HttpServletRequest, context: ActionContext): (String, Array[Byte]) = {
    var serializer: Serializer = null
    var mimeType: MediaType = null
    val mimeTypes = context.acceptTypes.iterator
    while (mimeTypes.hasNext && serializer == null) {
      mimeType = mimeTypes.next()
      serializer = viewManager.getSerializer(mimeType)
    }
    if (null != serializer) {
      val params = new collection.mutable.HashMap[String, Any]
      val enm = request.getAttributeNames
      while (enm.hasMoreElements) {
        val attr = enm.nextElement()
        params.put(attr, request.getAttribute(attr))
      }
      params ++= context.params
      val os = new ByteArrayOutputStream
      serializer.serialize(result.asInstanceOf[AnyRef], os, params.toMap)
      (contentTypeOf(mimeType), os.toByteArray)
    } else {
      // 无 Serializer：用 Accept 首项（或 text/plain）+ UTF-8 写出 toString
      val ct = contentTypeOf(context.acceptTypes.headOption.getOrElse(MediaTypes.text))
      (ct, result.toString.getBytes(Charsets.UTF_8))
    }
  }

  /** 仅文本型 MIME 附加 charset=UTF-8；二进制类型保持裸 MIME，避免误导客户端。 */
  private def contentTypeOf(mimeType: MediaType): String = {
    val base = mimeType.toString
    val textual = mimeType.primaryType == "text" ||
      mimeType.subType.contains("json") ||
      mimeType.subType.contains("xml") ||
      mimeType.subType.contains("javascript")
    if (textual) base + "; charset=UTF-8" else base
  }

  /**
   * 写出响应体，并补齐缓存指令。maxAgeSecond>0 时按声明式缓存设置 s-maxage，否则禁用缓存。
   *
   * 补齐而非改写：action 已自行声明 Cache-Control 时，整套缓存指令都交给它
   * （不再补 Pragma/Expires，它们与 no-store 是同一个决策），
   * 否则 action 的缓存策略会被这里静默丢弃。
   */
  private def writeToResponse(res: HttpServletResponse, contentType: String, data: Array[Byte], maxAgeSecond: Int): Unit = {
    res.setContentType(contentType)
    res.setContentLength(data.length)
    CacheControl.fillIfAbsent(res, maxAgeSecond)
    res.getOutputStream.write(data)
  }

  /** 命中缓存：先重放缓存时 action 写入的响应头，再写出响应体。 */
  private def writeCachedToResponse(res: HttpServletResponse, result: CacheResult, maxAgeSecond: Int): Unit = {
    result.headers.foreach { case (name, value) => res.setHeader(name, value) }
    writeToResponse(res, result.contentType, result.data, maxAgeSecond)
  }

  private def preHandle(interceptors: Array[Interceptor], context: ActionContext, request: HttpServletRequest, response: HttpServletResponse): Int = {
    var i = 0
    while (i < interceptors.length) {
      val interceptor = interceptors(i)
      if (!interceptor.preInvoke(request, response)) return i - 1
      i += 1
    }
    i - 1
  }

  private def postHandle(interceptors: Array[Interceptor], context: ActionContext, lastInterceptorIndex: Int, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    var i = lastInterceptorIndex
    while (i >= 0) {
      val interceptor = interceptors(i)
      interceptor.postInvoke(request, response)
      i -= 1
    }
  }
}
