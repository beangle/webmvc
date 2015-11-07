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
package org.beangle.webmvc.execution.impl

import org.beangle.commons.lang.annotation.description
import org.beangle.commons.security.{ DefaultRequest, Request }
import org.beangle.commons.web.security.RequestConvertor
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.execution.{ Handler, MappingHandler }

import javax.servlet.http.HttpServletRequest

class MvcRequestConvertor extends RequestConvertor {
  def convert(request: HttpServletRequest): Request = {
    val context = ActionContext.current
    Handler.current match {
      case amhandler: MappingHandler =>
        new DefaultRequest(amhandler.mapping.action.name, amhandler.mapping.method.getName)
      case _ =>
        new DefaultRequest(RequestUtils.getServletPath(context.request), context.request.getMethod)
    }
  }
}