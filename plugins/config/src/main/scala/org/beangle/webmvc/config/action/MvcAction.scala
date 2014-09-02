/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.config.action

import org.beangle.commons.io.{ IOs, ResourcePatternResolver }
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.reflect.BeanManifest
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.dispatch.impl.HierarchicalUrlMapper

/**
 * @author chaostone
 */
@description("Beange WebMVC 配置查看器")
class MvcAction extends ActionSupport {

  var mapper: HierarchicalUrlMapper = _

  var configurer: Configurer = _

  def index(): String = {
    put("namespaces", getNamespaces())
    forward()
  }

  def profiles(): String = {
    put("profiles", configurer.profiles)
    forward()
  }

  def actions(): String = {
    val namespace = get("namespace", "")
    put("namespace", namespace)
    put("actionNames", getActionNames(namespace))
    forward()
  }

  def action(): String = {
    val actionName = get("name", "")
    val config = mapper.antiResolve(actionName).get
    try {
      val clazz = config.clazz
      put("properties", BeanManifest.get(clazz).getters.values.filterNot(m => m.method.getName.contains("$") || m.method.getDeclaringClass == classOf[ActionSupport]))
    } catch {
      case e: Throwable =>
        error("Unable to get properties for action " + actionName, e)
        addError("Unable to retrieve action properties: " + e.toString())
    }
    put("config", config)
    put("mapping", config.mappings)
    forward()
  }
  private def getNamespaces(): Seq[String] = {
    val namespaces = new collection.mutable.HashSet[String]
    mapper.actionNames foreach { name =>
      namespaces += Strings.substringBeforeLast(name, "/")
    }
    namespaces.toList.sorted
  }

  private def getActionNames(namespace: String): Seq[String] = {
    val actionNames = new collection.mutable.HashSet[String]
    mapper.actionNames foreach { name =>
      if (name.startsWith(namespace) && !Strings.substringAfter(name, namespace + "/").contains("/"))
        actionNames += Strings.substringAfterLast(name, "/")
    }
    actionNames.toList.sorted
  }
}