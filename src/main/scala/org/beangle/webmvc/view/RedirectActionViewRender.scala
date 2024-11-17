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

import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.{Charsets, Strings}
import org.beangle.commons.net.http.HttpMethods
import org.beangle.webmvc.config.Configurator
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.{To, ToClass}

import java.net.URLEncoder

@description("重定向调转渲染者")
class RedirectActionViewRender(val configurator: Configurator) extends ViewRender {

  override def supportViewClass: Class[_] = {
    classOf[RedirectActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val request = context.request
    val response = context.response
    val url = new StringBuilder(toURL(view))

    var redirectParams = request.getParameter("_params")
    if (null != redirectParams) {
      if (redirectParams.nonEmpty && redirectParams.charAt(0) == '&') redirectParams = redirectParams.substring(1)
    }

    if (null != redirectParams) {
      val rpsb = new StringBuilder
      Strings.split(redirectParams, "&") foreach { pair =>
        val v = Strings.substringAfter(pair, "=")
        if (Strings.isNotBlank(v)) {
          val key = URLEncoder.encode(Strings.substringBefore(pair, "="), Charsets.UTF_8)
          rpsb.append(key).append('=')
          rpsb.append(URLEncoder.encode(v, Charsets.UTF_8))
          rpsb.append('&')
        }
      }
      if (rpsb.nonEmpty) {
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
    view.asInstanceOf[RedirectActionView].to match {
      case ca: ToClass =>
        configurator.getRouteMapping(ca.clazz, ca.method) match {
          case Some(am) =>
            if (am.httpMethod != HttpMethods.GET) throw new RuntimeException(s"Cannot redirect action mapping using ${am.httpMethod}")
            val ua = am.toURL(ca.parameters, ActionContext.current.params)
            ca.parameters --= am.urlParams.keys
            ua.params(ca.parameters)
            ua.url
          case None => throw new RuntimeException(s"Cannot find action mapping for ${ca.clazz.getName} ${ca.method}")
        }
      case ua: To => ua.url
    }
  }
}
