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
package org.beangle.webmvc.config

import java.lang.reflect.Method
import org.beangle.commons.net.http.HttpMethods.{ DELETE, HEAD, PUT }
import org.beangle.commons.lang.Strings.{ join, split }
import org.beangle.webmvc.api.action.ToURL
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
  import org.beangle.commons.net.http.HttpMethods.{ DELETE, GET, HEAD, POST, PUT }
  final val BrowserUnsupported = Map((PUT, "put"), (DELETE, "delete"), (HEAD, "head"))
}

class RouteMapping(val httpMethod: String, val action: ActionMapping, val method: Method, val name: String,
    val arguments: Array[Argument], val urlParams: Map[String, Integer], val defaultView: String) {

  def url: String = {
    if ("" == name) {
      action.name
    } else {
      if (action.name.endsWith("/")) action.name + name else (action.name + "/" + name)
    }
  }

  def fill(paramMaps: collection.Map[String, Any]*): String = {
    if (urlParams.isEmpty) return url
    val parts = split(url, '/')
    urlParams foreach {
      case (name, index) =>
        val iter = paramMaps.iterator
        var value: Option[Any] = None
        while (iter.hasNext && value == None) {
          value = iter.next.get(name)
        }
        parts(index) = String.valueOf(value.get)
    }
    "/" + join(parts, "/")
  }

  override def toString: String = {
    (if (null == httpMethod) "*" else httpMethod) + " " + url + " " + action.clazz.getName + "." +
      method.getName + "(" + join(arguments, ",") + ")"
  }

  def toURL(paramMaps: collection.Map[String, Any]*): ToURL = {
    val ua = new ToURL(fill(paramMaps: _*))
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
    (pathSegment.charAt(0) == '{' && pathSegment.charAt(pathSegment.length - 1) == '}')
  }
  /**
   * /a/b/c => ()
   * /{a}/&star/{c} => (a->0,1->1,c->2)
   * /a/b/{c}/{a*} => (c->2,a*->3)
   */
  def parse(pattern: String): Map[String, Integer] = {
    var parts = split(pattern, "/")
    var params = new collection.mutable.HashMap[String, Integer]
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
