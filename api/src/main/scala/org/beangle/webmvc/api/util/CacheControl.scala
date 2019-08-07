/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.api.util

import java.{util => ju}

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.webmvc.api.context.ActionContext

object CacheControl {

  def expiresAfter(minutes: Int, response: HttpServletResponse = ActionContext.current.response): this.type = {
    val cal = ju.Calendar.getInstance()
    cal.add(ju.Calendar.MINUTE, minutes)
    val expires = cal.getTimeInMillis
    response.setDateHeader("Date", System.currentTimeMillis())
    response.setDateHeader("Expires", expires)
    response.setDateHeader("Retry-After", expires)
    response.setHeader("Cache-Control", "max-age=3600, public")
    this
  }

  /**
   * return true if already has it's etag
   */
  def withEtag(etag: String, request: HttpServletRequest = ActionContext.current.request,
               response: HttpServletResponse = ActionContext.current.response): Boolean = {
    val requestETag = request.getHeader("If-None-Match")
    response.setHeader("ETag", etag)

    // not modified, content is not sent - only basic headers and status SC_NOT_MODIFIED
    if (etag.equals(requestETag)) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
      true
    } else false
  }
}
