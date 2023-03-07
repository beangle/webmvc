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

package org.beangle.webmvc.view.i18n

import java.util as ju
import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.{DefaultTextResource, TextBundleRegistry, TextFormater}
import org.beangle.web.action.context.ActionContext
import org.beangle.commons.text.i18n.TextProvider
import org.beangle.webmvc.action.EntitySupport
import org.beangle.webmvc.execution.MappingHandler

import scala.collection.mutable

class ActionTextResource(context: ActionContext, locale: ju.Locale, registry: TextBundleRegistry, formater: TextFormater)
  extends DefaultTextResource(locale, registry, formater) with TextProvider {

  /**
   * 1 remove index key(user.roles[0].name etc.)
   * 2 change ModelDriven to EntitySupport
   * 3 remove superclass and interface lookup
   */
  protected override def get(key: String): Option[String] = {
    if (key == null) return Some("")

    val handler = context.handler
    if (!handler.isInstanceOf[MappingHandler]) return None
    val amHander = handler.asInstanceOf[MappingHandler]
    val mapping = amHander.mapping

    val actionClass = mapping.action.clazz
    val checked = new mutable.HashSet[String]
    // search up class hierarchy
    var msg = getMessage(actionClass.getName, locale, key)
    if (msg.isDefined) return msg
    // nothing still? all right, search the package hierarchy now
    msg = getPackageMessage(actionClass, key, checked)
    if (msg.isDefined) return msg

    if (classOf[EntitySupport[_]].isAssignableFrom(actionClass)) {
      // search up model's class hierarchy
      val entityClass = mapping.action.action.asInstanceOf[EntitySupport[_]].entityClass
      if (entityClass != null) {
        val entityPrefix = entityClass.getSimpleName + "."
        if (Strings.capitalize(key).startsWith(entityPrefix)) {
          msg = getMessage(entityClass.getName, locale, key.substring(entityPrefix.length))
        }
        if (msg.isEmpty) msg = getPackageMessage(entityClass, key, checked)
        if (msg.isDefined) return msg
      }
    }

    // see if it's a child property
    var idx = key.indexOf(".")
    if (idx > 0) {
      var prop = key.substring(0, idx)
      val obj = context.attribute[Any](prop)
      if (null != obj && !prop.equals("action")) {
        var aClass: Class[_] = obj.getClass
        var newKey = key
        var goOn = true
        while (null != aClass && goOn && msg.isEmpty) {
          msg = getPackageMessage(aClass, newKey, checked)
          if (msg.isEmpty) {
            val nextIdx = newKey.indexOf(".", idx + 1)
            if (nextIdx == -1) {
              goOn = false
            } else {
              prop = newKey.substring(idx + 1, nextIdx)
              newKey = newKey.substring(idx + 1)
              idx = nextIdx
              aClass = if (Strings.isNotEmpty(prop)) Properties.getType(aClass, prop) else null
            }
          }
        }
      }
    }
    registry.getDefaultText(key, locale)
  }

  private def getPackageMessage(clazz: Class[_], key: String, checked: mutable.Set[String]): Option[String] = {
    var msg: Option[String] = None
    var baseName = clazz.getName
    while (baseName.lastIndexOf('.') != -1 && msg.isEmpty) {
      baseName = baseName.substring(0, baseName.lastIndexOf('.'))
      if (!checked.contains(baseName)) {
        msg = getMessage(baseName + ".package", locale, key)
        if (msg.isDefined) return msg
        checked += baseName
      }
    }
    None
  }

  /**
   * Gets the message from the named resource bundle.
   */
  private def getMessage(bundleName: String, locale: ju.Locale, key: String): Option[String] = {
    registry.load(locale, bundleName).get(key)
  }
}
