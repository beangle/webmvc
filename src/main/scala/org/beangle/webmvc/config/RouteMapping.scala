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

package org.beangle.webmvc.config

import org.beangle.commons.lang.Strings.{join, split}
import org.beangle.webmvc.ToURI
import org.beangle.webmvc.annotation.response
import org.beangle.webmvc.context.Argument

import java.lang.reflect.Method

object RouteMapping {
  final val DefaultMethod = "index"
  final val MethodParam = "_method"

  import org.beangle.commons.net.http.HttpMethods.{DELETE, HEAD, PUT}

  final val BrowserUnsupported = Map((PUT, "put"), (DELETE, "delete"), (HEAD, "head"))

  private def actionUrl(action: ActionMapping, name: String): String = {
    if ("" == name) {
      action.name
    } else {
      if (action.name.endsWith("/")) action.name + name else action.name + "/" + name
    }
  }

  private def isCacheMethod(method: Method): Boolean = {
    if (null == method) {
      false
    } else {
      val res = method.getAnnotation(classOf[response])
      if null == res then false else res.cacheable()
    }
  }

  def apply(httpMethod: String, action: ActionMapping, method: Method, name: String,
            arguments: Array[Argument], urlParams: Map[String, Integer], defaultView: String): RouteMapping = {
    new RouteMapping(httpMethod, action, method, actionUrl(action, name), arguments, urlParams, defaultView, isCacheMethod(method))
  }
}

class RouteMapping private(val httpMethod: String, val action: ActionMapping, val method: Method, val url: String,
                           val arguments: Array[Argument], val urlParams: Map[String, Integer], val defaultView: String,
                           val cacheable: Boolean) {

  def fill(paramMaps: collection.Map[String, Any]*): String = {
    if (urlParams.isEmpty) return url
    val parts = split(url, '/')
    urlParams foreach {
      case (name, index) =>
        val iter = paramMaps.iterator
        var value: Option[Any] = None
        while (iter.hasNext && value.isEmpty) {
          value = iter.next().get(name)
        }
        parts(index) = String.valueOf(value.get)
    }
    "/" + join(parts, "/")
  }

  override def toString: String = {
    (if (null == httpMethod) "*" else httpMethod) + " " + url + " " + action.clazz.getName + "." +
      method.getName + "(" + join(arguments, ",") + ")"
  }

  def toURL(paramMaps: collection.Map[String, Any]*): ToURI = {
    val ua = new ToURI(fill(paramMaps: _*))
    RouteMapping.BrowserUnsupported.get(this.httpMethod) foreach { m =>
      ua.param(RouteMapping.MethodParam, m)
    }
    ua
  }
}
