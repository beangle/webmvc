/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
import org.beangle.commons.http.HttpMethods.{ DELETE, HEAD, PUT }
import org.beangle.commons.lang.Strings.{ join, split }
import org.beangle.webmvc.api.action.ToURL
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.context.Argument

trait Configurer {

  def getProfile(className: String): Profile

  def profiles: Seq[Profile]

  def build(): Seq[Tuple3[String, ActionMapping, Object]]

  def actionConfigs: Map[String, ActionConfig]

  def getActionMapping(name: String, method: String): Option[ActionMapping]

  def getConfig(name: String): Option[ActionConfig]
}

/**
 * action config (namespace endwith /)
 */
class ActionConfig(val clazz: Class[_], val name: String, val namespace: String, val views: Map[String, View], val profile: Profile) {
  var mappings: Map[String, ActionMapping] = Map.empty
}

object ActionMapping {
  final val DefaultMethod = "index"
  final val MethodParam = "_method"
  import org.beangle.commons.http.HttpMethods.{ DELETE, GET, HEAD, POST, PUT }
  final val BrowserUnsupported = Map((PUT, "put"), (DELETE, "delete"), (HEAD, "head"))
}

class ActionMapping(val httpMethod: String, val config: ActionConfig, val method: Method, val name: String,
  val arguments: Array[Argument], val urlParams: Map[Integer, String], val defaultView: String) {

  def url = if ("" == name) config.name else (config.name + "/" + name)

  def fill(paramMaps: collection.Map[String, Any]*): String = {
    if (urlParams.isEmpty) return url
    val parts = split(url, '/')
    urlParams foreach {
      case (index, name) =>
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
    (if (null == httpMethod) "*" else httpMethod) + " " + url + " " + config.clazz.getName + "." +
      method.getName + "(" + join(arguments, ",") + ")"
  }

  def toURL(paramMaps: collection.Map[String, Any]*): ToURL = {
    val ua = new ToURL(fill(paramMaps: _*))
    ActionMapping.BrowserUnsupported.get(this.httpMethod) foreach { m =>
      ua.param(ActionMapping.MethodParam, m)
    }
    ua
  }
}
