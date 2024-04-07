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

package org.beangle.webmvc.inspect

import java.util.Locale
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.reflect.BeanInfos
import org.beangle.commons.text.i18n.Messages
import org.beangle.web.action.support.ActionSupport
import org.beangle.webmvc.config.{ ActionConfig, Configurer }
import org.beangle.webmvc.config.ActionMapping
import org.beangle.web.action.view.View

/**
 * @author chaostone
 */
@description("Beange WebMVC 配置查看器")
class MvcAction extends ActionSupport {

  var configurer: Configurer = _

  def index(): View = {
    put("namespaces", getNamespaces())
    forward()
  }

  def profiles(): View = {
    put("profiles", configurer.profiles)
    forward()
  }

  def actions(): View = {
    val namespace = get("namespace", "")
    put("namespace", namespace)
    put("actionNames", getActionNames(namespace))
    forward()
  }

  def action(): View = {
    val actionName = get("name", "")
    val mapping = configurer.getActionMapping(actionName).get
    try {
      val clazz = mapping.clazz
      put("properties", BeanInfos.get(clazz).properties.values.filterNot(m => m.name.contains("$")))
    } catch {
      case e: Throwable =>
        logger.error("Unable to get properties for action " + actionName, e)
        addError("Unable to retrieve action properties: " + e.toString())
    }
    put("mapping", mapping)
    put("mappings", mapping.mappings)
    forward()
  }

  def jekyll(): View = {
    val packageName = get("packageName", "")
    val actionNames = new collection.mutable.HashSet[String]
    val configs = configurer.actionMappings.values.toSet
    val descriptions = new collection.mutable.HashMap[String, String]
    val messages = Messages(Locale.SIMPLIFIED_CHINESE)
    val configMap = new collection.mutable.HashMap[String, ActionMapping]
    configs foreach { config =>
      if (config.clazz.getName.startsWith(packageName)) {
        val actionName = config.name
        actionNames += actionName
        descriptions.put(actionName, messages.get(config.clazz, "class"))
        configMap.put(actionName, config)
      }
    }
    put("messages", messages)
    put("configMap", configMap)
    put("actionNames", actionNames.toList.sorted)
    put("descriptions", descriptions)
    forward()
  }

  private def getNamespaces(): Seq[String] = {
    val configs = configurer.actionMappings.values.toSet
    configs.map(config => config.namespace).toList.sorted
  }

  private def getActionNames(namespace: String): Seq[String] = {
    val actionNames = new collection.mutable.HashSet[String]
    val configs = configurer.actionMappings.values.toSet
    configs foreach { config =>
      if (config.namespace == namespace) {
        if (config.name == namespace) actionNames += ""
        else actionNames += Strings.substringAfter(config.name, namespace + "/")
      }
    }
    actionNames.toList.sorted
  }
}
