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

import jakarta.servlet.http.HttpServletRequest
import org.beangle.template.api.*
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.dispatch.ActionUriRender
import org.beangle.webmvc.execution.MappingHandler

trait WebUIBean {
  self: ComponentContextAware =>

  final def request: HttpServletRequest = {
    ActionContext.current.request
  }

  final def requestURI: String = {
    request.getRequestURI
  }

  final def requestParameter(name: String): String = {
    request.getParameter(name)
  }

  final def render(uri: String): String = {
    context.services("uriRender").asInstanceOf[ActionUriRender].render(uri)
  }
}

class ActionUIBean(context: ComponentContext) extends UIBean(context) with WebUIBean

class ActionIterableUIBean(context: ComponentContext) extends IterableUIBean(context) with WebUIBean

class ActionClosingUIBean(context: ComponentContext) extends ClosingUIBean(context) with WebUIBean
