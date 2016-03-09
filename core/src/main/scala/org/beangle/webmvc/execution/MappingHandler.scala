/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.execution

import org.beangle.commons.io.Serializer
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.web.intercept.Interceptor
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.view.impl.ViewManager

import javax.activation.MimeType
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

/**
 * 缺省的调用反应堆
 * 负责调用Action,渲染结果
 */
@description("缺省的调用反应堆")
class MappingHandler(val mapping: RouteMapping, val invoker: Invoker, viewManager: ViewManager) extends ContextAwareHandler {

  override def handle(request: HttpServletRequest, response: HttpServletResponse): Any = {
    val action = mapping.action
    val interceptors = action.profile.interceptors
    val context = ActionContext.current
    val lastInterceptorIndex = preHandle(interceptors, context, request, response)
    try {
      if (lastInterceptorIndex == interceptors.length - 1) {
        var result = invoker.invoke()
        if (null == result) result = mapping.defaultView
        val view = result match {
          case null => null
          case viewName: String =>
            action.views.get(viewName) match {
              case Some(v) => v
              case None =>
                val profile = action.profile
                viewManager.getResolver(profile.viewType) match {
                  case Some(resolver) =>
                    var i = 0
                    val candidates = Strings.split(viewName, ",")
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
          case _          => null
        }

        if (null != view) {
          viewManager.getRender(view.getClass) match {
            case Some(render) => render.render(view, context)
            case None         => throw new RuntimeException(s"Cannot find render for ${view.getClass}")
          }
        } else {
          if (null != viewManager.contentNegotiationManager) {
            val mimeTypes = viewManager.contentNegotiationManager.resolve(request).iterator
            var serializer: Serializer = null
            var mimeType: MimeType = null
            while (mimeTypes.hasNext && serializer == null) {
              mimeType = mimeTypes.next()
              serializer = viewManager.getSerializer(mimeType)
            }
            if (null != serializer) {
              response.setCharacterEncoding("UTF-8")
              response.setContentType(mimeType.toString + "; charset=UTF-8")
              val params = new collection.mutable.HashMap[String, Any]
              val enum = request.getAttributeNames
              while (enum.hasMoreElements()) {
                val attr = enum.nextElement()
                params.put(attr, request.getAttribute(attr))
              }
              params ++= context.params
              serializer.serialize(result.asInstanceOf[AnyRef], response.getOutputStream, params.toMap)
            }
          }
        }
      }
    } finally {
      //FIXME process exception
      postHandle(interceptors, context, lastInterceptorIndex, request, response)
    }
  }

  def preHandle(interceptors: Array[Interceptor], context: ActionContext, request: HttpServletRequest, response: HttpServletResponse): Int = {
    var i = 0
    while (i < interceptors.length) {
      val interceptor = interceptors(i)
      if (!interceptor.preInvoke(request, response)) return i - 1
      i += 1
    }
    i - 1
  }

  def postHandle(interceptors: Array[Interceptor], context: ActionContext, lastInterceptorIndex: Int, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    var i = lastInterceptorIndex
    while (i >= 0) {
      val interceptor = interceptors(i)
      interceptor.postInvoke(request, response)
      i -= 1
    }
  }
}
  