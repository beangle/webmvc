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

import org.beangle.web.action.context.ActionContext
import org.beangle.template.api.ComponentContext
import org.beangle.template.api.{UIBean,ClosingUIBean}

class Messages(context: ComponentContext) extends UIBean(context) {
  var actionMessages: List[String] = _
  var actionErrors: List[String] = _

  var clear = "true"

  override def evaluateParams(): Unit = {
    actionMessages = ActionContext.current.flash.messages
    actionErrors = ActionContext.current.flash.errors

    if (actionMessages.nonEmpty || actionErrors.nonEmpty) {
      generateIdIfEmpty()
      if ("true".equals(clear)) ActionContext.current.flash.clear()
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
