/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.config

import java.lang.reflect.Method

import org.beangle.commons.lang.Strings.{join, split}
import org.beangle.webmvc.api.action.ToURI
import org.beangle.webmvc.api.annotation.response
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.context.Argument

trait Configurer {

  def getProfile(className: String): Profile

  def profiles: Seq[Profile]

  def build(): Unit

  def actionMappings: Map[String, ActionMapping]

  def getRouteMapping(clazz: Class[_], method: String): Option[RouteMapping]

  def getActionMapping(name: String): Option[ActionMapping]
}

class ActionConfig(val url: String, val mapping: RouteMapping, val action: Object)

/**
 * action mapping (namespace endwith /)
 * name is action fullname ,so it starts with /,and contains namespace
 */
class ActionMapping(val action: AnyRef, val clazz: Class[_], val name: String, val namespace: String, val views: Map[String, View], val profile: Profile) {
  var mappings: Map[String, RouteMapping] = Map.empty
}

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

  def apply(httpMethod: String, action: ActionMapping, method: Method, name: String,
            arguments: Array[Argument], urlParams: Map[String, Integer], defaultView: String): RouteMapping = {
    val res = method.getAnnotation(classOf[response])
    val cacheable = if (null == res) {
      false
    } else {
      res.cacheable()
    }
    new RouteMapping(httpMethod, action, method, actionUrl(action, name), arguments, urlParams, defaultView, cacheable)
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

object Path {

  def isTailMatch(path: String): Boolean = {
    path.charAt(path.length - 1) == '*'
  }

  def isTailPattern(path: String): Boolean = {
    path.endsWith("*}")
  }

  def isPattern(pathSegment: String): Boolean = {
    pathSegment.charAt(0) == '{' && pathSegment.charAt(pathSegment.length - 1) == '}'
  }

  /**
   * /a/b/c => ()
   * /{a}/&star/{c} => (a->0,1->1,c->2)
   * /a/b/{c}/{a*} => (c->2,a*->3)
   */
  def parse(pattern: String): Map[String, Integer] = {
    val parts = split(pattern, "/")
    val params = new collection.mutable.HashMap[String, Integer]
    var i = 0
    while (i < parts.length) {
      val p = parts(i)
      if (p.charAt(0) == '{' && p.charAt(p.length - 1) == '}') {
        params.put(p.substring(1, p.length - 1), Integer.valueOf(i))
      } else if (p == "*") {
        params.put(String.valueOf(i), Integer.valueOf(i))
      }
      i += 1
    }
    params.toMap
  }
}
