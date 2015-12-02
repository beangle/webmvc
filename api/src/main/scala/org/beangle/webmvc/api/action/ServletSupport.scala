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
package org.beangle.webmvc.api.action

import org.beangle.commons.web.util.{CookieUtils, RequestUtils}
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.ActionContext

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

trait ServletSupport {

  @ignore
  protected final def request: HttpServletRequest = ActionContext.current.request

  @ignore
  protected final def response: HttpServletResponse = ActionContext.current.response

  protected final def getCookieValue(cookieName: String): String = {
    CookieUtils.getCookieValue(request, cookieName)
  }

  protected final def addCookie(name: String, value: String, path: String, age: Int) {
    CookieUtils.addCookie(request, response, name, value, path, age)
  }

  protected final def addCookie(name: String, value: String, age: Int) {
    CookieUtils.addCookie(request, response, name, value, age)
  }

  protected final def deleteCookie(name: String) {
    CookieUtils.deleteCookieByName(request, response, name)
  }

  @ignore
  protected def remoteAddr: String = RequestUtils.getIpAddr(request)

}