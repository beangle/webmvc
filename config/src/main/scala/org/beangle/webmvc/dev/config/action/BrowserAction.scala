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
package org.beangle.webmvc.dev.config.action

import org.beangle.commons.io.{ IOs, ResourcePatternResolver }
import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.lang.reflect.BeanManifest
import org.beangle.webmvc.api.action.{ ActionSupport, to }
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.dispatch.impl.HierarchicalUrlMapper
import org.beangle.webmvc.helper.ContainerHelper
import org.beangle.webmvc.config.Configurer

/**
 * @author chaostone
 */
class BrowserAction extends ActionSupport {

  var mapper: HierarchicalUrlMapper = _

  var configurer: Configurer = _

  def index(): String = {
    put("profiles", configurer.profiles)
    forward()
  }
  
  def getNamespaces(): Seq[String] = {
    val namespaces = new collection.mutable.HashSet[String]
    mapper.actionNames foreach { name =>
      namespaces += Strings.substringBeforeLast(name, "/")
    }
    namespaces.toList.sorted
  }

  def getActionNames(namespace: String): Seq[String] = {
    val actionNames = new collection.mutable.HashSet[String]
    mapper.actionNames foreach { name =>
      if (name.startsWith(namespace) && !Strings.substringAfter(name, namespace + "/").contains("/"))
        actionNames += Strings.substringAfterLast(name, "/")
    }
    actionNames.toList.sorted
  }

  def actions(): String = {
    get("namespace") match {
      case Some(namespace) =>
        put("namespace", namespace)
        put("actionNames", getActionNames(namespace))
      case None =>
        put("namespaces", getNamespaces())
    }
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

  def beans(): String = {
    val container = ContainerHelper.get
    put("beanNames", container.keys)
    put("container", container)
    forward()
  }

  def jars(): String = {
    val resolver = new ResourcePatternResolver
    val urls = resolver.getResources("classpath*:META-INF/maven/**/pom.properties")
    val poms = new collection.mutable.ListBuffer[Map[String, String]]
    urls foreach { url =>
      poms += IOs.readJavaProperties(url)
    }
    put("jarPoms", poms.toList)
    put("pluginsLoaded", ClassLoaders.getResources("META-INF/beangle/web-module.properties"))
    forward()
  }

  def stripPackage(clazz: String): String = {
    clazz.substring(clazz.lastIndexOf('.') + 1)
  }

  def stripPackage(clazz: Class[_]): String = {
    clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1)
  }

}