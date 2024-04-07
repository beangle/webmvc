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

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.{ClassTextFinder, DefaultTextResource, TextBundleRegistry, TextFormatter}
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.support.EntitySupport
import org.beangle.webmvc.config.ActionMapping
import org.beangle.webmvc.execution.MappingHandler

import java.util as ju
import scala.collection.mutable

class ActionTextResource(context: ActionContext, action: ActionMapping, locale: ju.Locale, registry: TextBundleRegistry, formatter: TextFormatter, cache: ActionTextCache)
  extends DefaultTextResource(locale, registry, formatter) {

  /** Get text by key,don't apply format by variables
    * 1 get text by action and package
    * 2 get text according EntitySupport's entity class
    * 3 get text by superclass and interfaces
    */
  protected override def get(key: String): Option[String] = {
    if (key == null) return Some("")
    val actionClass = action.clazz
    var msg: Option[String] = None

    if null != cache then
      msg = cache.getText(actionClass, key)
      if (msg.isDefined) return msg

    msg = find(actionClass, key)

    if (msg.isEmpty) {
      msg = registry.getDefaultText(key, locale)
      if msg.nonEmpty && null != cache then cache.update(actionClass, key, msg.get, true)
    } else {
      if null != cache then cache.update(actionClass, key, msg.get, false)
    }
    msg
  }

  private def find(actionClass: Class[_], key: String): Option[String] = {
    // search up class hierarchy
    var msg = new ClassTextFinder(locale, registry).find(actionClass, key)
    if (msg.isDefined) return msg

    if (classOf[EntitySupport[_]].isAssignableFrom(actionClass)) {
      // search up model's class hierarchy
      val entityClass = action.action.asInstanceOf[EntitySupport[_]].entityClass
      if (entityClass != null) {
        val entityPrefix = entityClass.getSimpleName + "."
        if (Strings.capitalize(key).startsWith(entityPrefix)) {
          msg = getPropertyMessage(entityClass, key.substring(entityPrefix.length))
        }
        if (msg.isDefined) return msg
      }
    }
    // see if it's a child property
    val idx = key.indexOf(".")
    if (idx > 0) {
      val first = key.substring(0, idx)
      val obj = context.attribute[Any](first)
      if (null != obj) {
        msg = getPropertyMessage(obj.getClass, key.substring(idx + 1))
      }
    }
    msg
  }

  private def getPropertyMessage(clazz: Class[_], key: String): Option[String] = {
    var aClass = clazz
    var newKey = key
    var goOn = true
    var msg: Option[String] = None
    var idx = -1
    while (null != aClass && goOn && msg.isEmpty) {
      msg = new ClassTextFinder(locale, registry).find(aClass, newKey)
      if (msg.isEmpty) {
        val nextIdx = newKey.indexOf(".", idx + 1)
        if (nextIdx == -1) {
          goOn = false
        } else {
          val prop = newKey.substring(idx + 1, nextIdx)
          newKey = newKey.substring(nextIdx + 1)
          idx = nextIdx
          aClass = if (Strings.isNotEmpty(prop)) Properties.getType(aClass, prop) else null
        }
      }
    }
    msg
  }

}
