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
package org.beangle.webmvc.api.action

import org.beangle.webmvc.api.context.Flash
import org.beangle.webmvc.api.context.ActionContextHolder
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.api.context.ActionMessages
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.commons.lang.Chars

trait MessageSupport {

  final def getText(aTextName: String): String = {
    ActionContextHolder.context.textProvider match {
      case Some(p) => p(aTextName).get
      case None    => aTextName
    }
  }

  final def getText(key: String, defaultValue: String, args: Any*): String = {
    ActionContextHolder.context.textProvider match {
      case Some(p) => p(key, defaultValue, args: _*)
      case None    => defaultValue
    }
  }

  protected final def getTextInternal(msgKey: String, args: Any*): String = {
    if (Strings.isEmpty(msgKey)) null
    if (Chars.isAsciiAlpha(msgKey.charAt(0)) && msgKey.indexOf('.') > 0) {
      getText(msgKey, msgKey, args: _*)
    } else {
      msgKey
    }
  }

  protected final def addMessage(msgKey: String, args: Any*): Unit = {
    ActionContextHolder.context.flash.addMessageNow(getTextInternal(msgKey, args: _*))
  }

  protected final def addError(msgKey: String, args: Any*): Unit = {
    ActionContextHolder.context.flash.addErrorNow(getTextInternal(msgKey, args: _*))
  }

  protected final def addFlashError(msgKey: String, args: Any*): Unit = {
    ActionContextHolder.context.flash.addError(getTextInternal(msgKey, args: _*))
  }

  protected final def addFlashMessage(msgKey: String, args: Any*): Unit = {
    ActionContextHolder.context.flash.addMessage(getTextInternal(msgKey, args: _*))
  }

  /**
   * 获得action消息<br>
   */
  @ignore
  protected final def actionMessages: List[String] = {
    val messages = ActionContextHolder.context.flash.get(Flash.MESSAGES).asInstanceOf[ActionMessages]
    if (null == messages) List()
    else messages.messages.toList
  }

  /**
   * 获得aciton错误消息<br>
   */
  @ignore
  protected final def actionErrors: List[String] = {
    val messages = ActionContextHolder.context.flash.get(Flash.MESSAGES).asInstanceOf[ActionMessages]
    if (null == messages) List()
    else messages.errors.toList
  }

}