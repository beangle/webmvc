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
import org.beangle.commons.activation.MediaType
import org.beangle.commons.io.Serializer
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.view.{PathView, View}
import org.beangle.web.servlet.intercept.Interceptor
import org.beangle.web.servlet.resource.PathResolver
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.view.impl.ViewManager

import java.io.ByteArrayOutputStream

/**
 * 缺省的调用处理器
 * 负责调用Action,渲染结果
 */
@description("缺省的调用处理器")
class MappingHandler(val mapping: RouteMapping, val invoker: Invoker,
                     viewManager: ViewManager,
                     responseCache: ResponseCache) extends ContextAwareHandler {

  override def handle(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val action = mapping.action
    if (mapping.cacheable) {
      responseCache.get(request) match {
        case Some(cr) =>
          writeToResponse(response, cr.contentType, cr.data, Some(15))
          return
        case None =>
      }
    }
    val interceptors = action.profile.interceptors
    val context = ActionContext.current
    val lastInterceptorIndex = preHandle(interceptors, context, request, response)
    try {
      if (lastInterceptorIndex == interceptors.length - 1) {
        val result = invoker.invoke()
        val flash = context.getFlash(false)
        if (null != flash) flash.writeNextToCookie()
        val view = result match {
          case null => null
          case PathView(path) =>
            val viewName = if (null == path) mapping.defaultView else path
            action.views.get(viewName) match {
              case Some(v) => v
              case None =>
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
          case view: View => view
          case _ => null
        }

        if (null != view) {
          viewManager.getRender(view.getClass) match {
            case Some(render) => render.render(view, context)
            case None => throw new RuntimeException(s"Cannot find render for ${view.getClass}")
          }
        } else if (null != result) {
          if (null != viewManager.contentNegotiationManager) {
            val mimeTypes = viewManager.contentNegotiationManager.resolve(request).iterator
            var serializer: Serializer = null
            var mimeType: MediaType = null
            while (mimeTypes.hasNext && serializer == null) {
              mimeType = mimeTypes.next()
              serializer = viewManager.getSerializer(mimeType)
            }
            if (null != serializer) {
              response.setCharacterEncoding("UTF-8")
              val contentType = mimeType.toString + "; charset=UTF-8"
              val params = new collection.mutable.HashMap[String, Any]
              val enm = request.getAttributeNames
              while (enm.hasMoreElements) {
                val attr = enm.nextElement()
                params.put(attr, request.getAttribute(attr))
              }
              params ++= context.params
              val os = new ByteArrayOutputStream
              serializer.serialize(result.asInstanceOf[AnyRef], os, params.toMap)
              val bytes = os.toByteArray

              if (context.handler.asInstanceOf[MappingHandler].mapping.cacheable) {
                responseCache.put(request, contentType, bytes)
                writeToResponse(response, contentType, bytes, Some(15))
              } else {
                writeToResponse(response, contentType, bytes, None)
              }
            }
          }
        }
      }
    } finally {
      //FIXME process exception
      postHandle(interceptors, context, lastInterceptorIndex, request, response)
    }
  }

  private def writeToResponse(res: HttpServletResponse, contentType: String, data: Array[Byte], maxAgeSecond: Option[Int]): Unit = {
    res.setContentType(contentType)
    res.setContentLength(data.length)
    maxAgeSecond foreach { maxAge =>
      if (maxAge <= 0) {
        res.addHeader("Cache-Control", "no-store")
      } else {
        res.addHeader("Cache-Control", s"public,s-maxage=${maxAge}")
      }
    }
    res.getOutputStream.write(data)
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
