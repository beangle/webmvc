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

package org.beangle.webmvc.view

import jakarta.servlet.http.HttpServletResponse._

object Status {

  def apply(code: Int): View = {
    new StatusView(code)
  }

  val Ok: View = this (SC_OK)

  val NotFound: View = this (SC_NOT_FOUND)

  val NotModified: View = this (SC_NOT_MODIFIED)

  val BadRequest: View = this (SC_BAD_REQUEST)

  val Forbidden: View = this (SC_FORBIDDEN)
}

class StatusView(val code: Int) extends View
