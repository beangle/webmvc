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
package org.beangle.webmvc.api.util

import org.beangle.webmvc.api.context.ActionContext
import java.{ util => ju }

object CacheControl {

  def expiresAfter(minutes: Int): this.type = {
    val response = ActionContext.current.response
    val cal = ju.Calendar.getInstance()
    cal.add(ju.Calendar.MINUTE, minutes)
    val expires = cal.getTimeInMillis()
    response.setDateHeader("Date", System.currentTimeMillis())
    response.setDateHeader("Expires", expires)
    response.setDateHeader("Retry-After", expires)
    response.setHeader("Cache-Control", "public")
    this
  }
}