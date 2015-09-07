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
package org.beangle.webmvc.api.view

import javax.servlet.http.HttpServletResponse.{ SC_BAD_REQUEST, SC_FORBIDDEN, SC_NOT_FOUND, SC_NOT_MODIFIED, SC_OK }

object Status {

  def apply(code: Int): View = {
    new StatusView(code)
  }
  val Ok = this(SC_OK)

  val NotFound = this(SC_NOT_FOUND)

  val NotModified = this(SC_NOT_MODIFIED)

  val BadRequest = this(SC_BAD_REQUEST)

  val Forbidden = this(SC_FORBIDDEN)
}

class StatusView(val code: Int) extends View 