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

package org.beangle.webmvc.support

import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.web.servlet.url.UrlBuilder

trait JsonAPISupport extends ServletSupport {

  var uriRender: ActionUriRender = _

  def uri(str: String): String = {
    uriRender.render(str)
  }

  def url(str: String): String = {
    val a = UrlBuilder(request)
    a.setRequestURI(uri(str))
    a.setQueryString(null)
    a.buildUrl()
  }
}
