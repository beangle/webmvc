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

package org.beangle.webmvc.dispatch

import jakarta.servlet.http.HttpServletRequest
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.security.{DefaultRequest, Request}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.execution.MappingHandler
import org.beangle.web.servlet.security.RequestConvertor
import org.beangle.web.servlet.util.RequestUtils

@description("基于Mvc的请求转换器")
class MvcRequestConvertor extends RequestConvertor {
  def convert(request: HttpServletRequest): Request = {
    val context = ActionContext.current
    context.handler match {
      case amhandler: MappingHandler =>
        new DefaultRequest(amhandler.mapping.action.name, amhandler.mapping.method.getName)
      case _ =>
        new DefaultRequest(RequestUtils.getServletPath(context.request), context.request.getMethod)
    }
  }
}
