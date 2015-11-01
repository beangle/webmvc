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
package org.beangle.webmvc.view.i18n

import java.{ util => jl }

import scala.collection.mutable.{ HashSet, Set }

import org.beangle.commons.bean.Properties
import org.beangle.commons.i18n.{ DefaultTextResource, TextBundleRegistry, TextFormater }
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.api.action.EntitySupport
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.i18n.TextProvider
import org.beangle.webmvc.execution.{ ActionHandler, MappingHandler }

class ActionTextResource(context: ActionContext, locale: jl.Locale, registry: TextBundleRegistry, formater: TextFormater)
    extends DefaultTextResource(locale, registry, formater) with TextProvider {

  /**
   * 1 remove index key(user.roles[0].name etc.)
   * 2 change ModelDriven to EntitySupport
   * 3 remove superclass and interface lookup
   */
  protected override def get(key: String): Option[String] = {
    if (key == null) ""

    val handler = ActionHandler.handler
    if (!handler.isInstanceOf[MappingHandler]) return None;
    val amHander = handler.asInstanceOf[MappingHandler]
    val mapping = amHander.mapping

    val actionClass = mapping.action.clazz
    var checked = new HashSet[String]
    // search up class hierarchy
    var msg = getMessage(actionClass.getName, locale, key)
    if (msg != None) return msg
    // nothing still? all right, search the package hierarchy now
    msg = getPackageMessage(actionClass, key, checked)
    if (msg != None) return msg

    if (classOf[EntitySupport[_]].isAssignableFrom(actionClass)) {
      // search up model's class hierarchy
      val entityType = mapping.action.action.asInstanceOf[EntitySupport[_]].entityType
      if (entityType != null) {
        msg = getPackageMessage(entityType, key, checked)
        if (msg != None) return msg
      }
    }

    // see if it's a child property
    var idx = key.indexOf(".")
    if (idx > 0) {
      var prop = key.substring(0, idx)
      var obj = context.attribute[Any](prop)
      if (null != obj && !prop.equals("action")) {
        var aClass: Class[_] = obj.getClass
        var newKey = key
        var goOn = true
        while (null != aClass && goOn && msg.isEmpty) {
          msg = getPackageMessage(aClass, newKey, checked)
          if (msg.isEmpty) {
            var nextIdx = newKey.indexOf(".", idx + 1)
            if (nextIdx == -1) {
              goOn = false
            } else {
              prop = newKey.substring(idx + 1, nextIdx)
              newKey = newKey.substring(idx + 1)
              idx = nextIdx
              if (Strings.isNotEmpty(prop)) aClass = Properties.getType(aClass, prop)
              else aClass = null
            }
          }
        }
      }
    }
    registry.getDefaultText(key, locale)
  }

  private def getPackageMessage(clazz: Class[_], key: String, checked: Set[String]): Option[String] = {
    var msg: Option[String] = None
    var baseName = clazz.getName
    while (baseName.lastIndexOf('.') != -1 && msg.isEmpty) {
      baseName = baseName.substring(0, baseName.lastIndexOf('.'))
      if (!checked.contains(baseName)) {
        msg = getMessage(baseName + ".package", locale, key)
        if (!msg.isEmpty) return msg
        checked += baseName
      }
    }
    None
  }

  /**
   * Gets the message from the named resource bundle.
   */
  private def getMessage(bundleName: String, locale: jl.Locale, key: String): Option[String] = {
    var bundle = registry.load(locale, bundleName)
    if (null == bundle) None else bundle.get(key)
  }
}