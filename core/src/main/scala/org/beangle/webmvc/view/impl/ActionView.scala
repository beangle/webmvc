/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.webmvc.view.impl

import org.beangle.commons.http.HttpMethods
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.api.action.{ ToClass, ToURL, To }
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.{ ActionView, ForwardActionView, RedirectActionView, View }
import org.beangle.webmvc.config.{ RouteMapping, Configurer }
import org.beangle.webmvc.view.{ TypeViewBuilder, ViewRender }

import javax.servlet.http.HttpServletRequest
import org.beangle.commons.lang.Strings
import java.net.URLEncoder

@description("前向调转视图构建者")
class ForwardActionViewBuilder extends TypeViewBuilder {

  override def build(view: view): View = {
    new ForwardActionView(To(view.location))
  }

  override def supportViewType: String = {
    "chain"
  }
}

@description("重定向调转视图构建者")
class RedirectActionViewBuilder extends TypeViewBuilder {

  override def build(view: view): View = {
    new RedirectActionView(To(view.location))
  }

  override def supportViewType: String = {
    "redirectAction"
  }
}

@description("前向调转渲染者")
class ForwardActionViewRender(val configurer: Configurer) extends ViewRender {

  override def supportViewClass: Class[_] = {
    classOf[ForwardActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    context.request.getRequestDispatcher(toURL(view, context.request)).forward(context.request, context.response)
  }

  final def toURL(view: View, request: HttpServletRequest): String = {
    view.asInstanceOf[ActionView].to match {
      case ca: ToClass =>
        configurer.getRouteMapping(ca.clazz, ca.method) match {
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
      case ua: ToURL => ua.url
    }
  }
}

@description("重定向调转渲染者")
class RedirectActionViewRender(val configurer: Configurer) extends ViewRender {

  override def supportViewClass: Class[_] = {
    classOf[RedirectActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val request = context.request
    val response = context.response
    val url = new StringBuilder(toURL(view))

    var redirectParams = request.getParameter("_params")
    val ajaxHead = "x-requested-with"
    if (null != redirectParams) {
      if (redirectParams.charAt(0) == '&') redirectParams = redirectParams.substring(1)
      if (null != request.getHeader(ajaxHead)) redirectParams += "&x-requested-with=1"
    } else if (null != request.getHeader(ajaxHead)) redirectParams = "x-requested-with=1"

    if (null != redirectParams) {
      val rpsb = new StringBuilder
      Strings.split(redirectParams, "&") foreach { pair =>
        val v = Strings.substringAfter(pair, "=")
        if (Strings.isNotBlank(v)) {
          rpsb.append(Strings.substringBefore(pair, "=")).append('=')
          rpsb.append(URLEncoder.encode(v, "utf-8"))
          rpsb.append('&')
        }
      }
      if (rpsb.length > 0) {
        rpsb.deleteCharAt(rpsb.length - 1)
        redirectParams = rpsb.toString
        if (url.contains('?')) url.append("&").append(redirectParams)
        else url.append("?").append(redirectParams)
      }
    }
    if (url.startsWith("http://") || url.startsWith("https://")) {
      response.sendRedirect(url.toString)
    } else {
      val finalLocation = if (request.getContextPath.length > 1) request.getContextPath + url.toString else url.toString
      val encodedLocation = response.encodeRedirectURL(finalLocation)
      response.sendRedirect(encodedLocation)
    }
  }

  final def toURL(view: View): String = {
    view.asInstanceOf[ActionView].to match {
      case ca: ToClass =>
        configurer.getRouteMapping(ca.clazz, ca.method) match {
          case Some(am) =>
            if (am.httpMethod != HttpMethods.GET) throw new RuntimeException(s"Cannot redirect action mapping using ${am.httpMethod}")
            val ua = am.toURL(ca.parameters, ActionContext.current.params)
            ca.parameters --= am.urlParams.keys
            ua.params(ca.parameters)
            ua.url
          case None => throw new RuntimeException(s"Cannot find action mapping for ${ca.clazz.getName} ${ca.method}")
        }
      case ua: ToURL => ua.url
    }
  }
}
