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
package org.beangle.webmvc.context.impl

import java.{ util => ju }

import org.beangle.commons.lang.Locales
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.context.LocaleResolver

import jakarta.servlet.http.{ HttpServletRequest, HttpServletResponse }

@description("基于参数的Locale解析器")
class ParamLocaleResolver extends LocaleResolver {
  val SessionAttribute = "WW_TRANS_I18N_LOCALE"
  val SessionParameter = "session_locale"
  val RequestParameter = "request_locale"

  override def resolve(request: HttpServletRequest): ju.Locale = {
    var locale: ju.Locale = request.getAttribute("locale").asInstanceOf[ju.Locale]
    if (null == locale) {
      // get session locale
      var session = request.getSession(false)
      if (null != session) {
        var session_locale = request.getParameter(SessionParameter)
        if (null == session_locale) {
          locale = session.getAttribute(SessionAttribute).asInstanceOf[ju.Locale]
        } else {
          locale = Locales.toLocale(session_locale)
          // save it in session
          session.setAttribute(SessionAttribute, locale)
        }
      }
      // get request locale
      var request_locale = request.getParameter(RequestParameter)
      if (null != request_locale) locale = Locales.toLocale(request_locale)

      if (null == locale) locale = request.getLocale
      request.setAttribute("locale", locale)
    }
    locale
  }
  override def setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: ju.Locale): Unit = {
    val session = request.getSession(false)
    if (null != session) session.setAttribute(SessionAttribute, locale)
  }
}
