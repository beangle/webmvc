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

package org.beangle.webmvc.view.impl

import org.beangle.webmvc.view.ViewRender
import org.beangle.commons.logging.Logging
import org.beangle.commons.lang.annotation.description
import org.beangle.web.action.view.StatusView
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.view.View

@description("HTTP状态渲染者")
class StatusViewRender extends ViewRender with Logging {
  override def supportViewClass: Class[_] = {
    classOf[StatusView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    context.response.setStatus(view.asInstanceOf[StatusView].code)
  }
}
