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

package org.beangle.webmvc.view.tag

import org.beangle.commons.lang.Strings
import org.beangle.template.api.{ClosingUIBean, ComponentContext, UIBean}
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.support.MessageSupport

class Messages(context: ComponentContext) extends UIBean(context) {
  var actionMessages: List[String] = _
  var actionErrors: List[String] = _

  var clear = "true"

  private def fetchMessages(Key: String): List[String] = {
    val flash = ActionContext.current.getFlash(true)
    flash.get(Key) match {
      case Some(m) => Strings.split(m, ';').toList
      case None => List.empty
    }
  }

  override def evaluateParams(): Unit = {
    actionMessages = fetchMessages(MessageSupport.MessagesKey)
    actionErrors = fetchMessages(MessageSupport.ErrorsKey)

    if (actionMessages.nonEmpty || actionErrors.nonEmpty) {
      generateIdIfEmpty()
      if ("true".equals(clear)) ActionContext.current.clearFlash()
    }
  }

  def hasErrors: Boolean = actionErrors.nonEmpty

  def hasMessages: Boolean = actionMessages.nonEmpty

}

class Dialog(context: ComponentContext) extends ActionClosingUIBean(context) {
  var title: String = _
  var href: String = _
  var modal = "false"

  override def evaluateParams(): Unit = {
    this.href = render(href)
  }
}
