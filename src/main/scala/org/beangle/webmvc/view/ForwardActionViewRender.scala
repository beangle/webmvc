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

package org.beangle.webmvc.view

import jakarta.servlet.http.HttpServletRequest
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.net.http.HttpMethods
import org.beangle.webmvc.config.{Configurator, RouteMapping}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.{To, ToClass}

@description("前向调转渲染者")
class ForwardActionViewRender(val configurator: Configurator) extends ViewRender {

  override def supportViewClass: Class[_] = {
    classOf[ForwardActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    context.request.getRequestDispatcher(toURL(view, context.request)).forward(context.request, context.response)
  }

  final def toURL(view: View, request: HttpServletRequest): String = {
    view.asInstanceOf[ForwardActionView].to match {
      case ca: ToClass =>
        configurator.getRouteMapping(ca.clazz, ca.method) match {
          case Some(am) =>
            if (am.httpMethod != HttpMethods.GET && am.httpMethod != HttpMethods.POST)
              throw new RuntimeException(s"Cannot forward action mapping using ${am.httpMethod}")
            val ua = am.toURL(ca.parameters, ActionContext.current.params)
            ca.parameters --= am.urlParams.keys
            ua.params(ca.parameters)
            if (am.httpMethod != request.getMethod) ua.param(RouteMapping.MethodParam, am.httpMethod)
            ua.url
          case None => throw new RuntimeException(s"Cannot find action mapping for ${ca.clazz.getName} ${ca.method}")
        }
      case ua: To => ua.url
      case null => throw new RuntimeException(s"Unsupported action view ${view.asInstanceOf[ForwardActionView].to}")
    }
  }
}
