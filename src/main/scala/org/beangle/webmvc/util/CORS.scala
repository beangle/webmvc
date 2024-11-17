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

package org.beangle.webmvc.util

import org.beangle.webmvc.context.ActionContext

/**
 * Cross Origin Resource Sharing
 * @see http://www.w3.org/TR/cors/
 */
object CORS {

  def allow(origins: String): this.type = {
    allow(origins, null, null)
  }

  def allow(origins: String, methods: String, headers: String): this.type = {
    val response = ActionContext.current.response
    response.addHeader("Access-Control-Allow-Origin", origins)
    if (null != methods) response.addHeader("Access-Control-Allow-Methods", methods)
    if (null != headers) response.addHeader("Access-Control-Allow-Headers", headers)
    this
  }

  def expose(header: String): this.type = {
    val response = ActionContext.current.response
    response.addHeader("Access-Control-Expose-Headers", header)
    this
  }

  def maxage(age: Int): this.type = {
    val response = ActionContext.current.response
    response.addIntHeader("Access-Control-Max-Age", age)
    this
  }
}
