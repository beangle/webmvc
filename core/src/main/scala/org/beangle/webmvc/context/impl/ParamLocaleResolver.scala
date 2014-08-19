package org.beangle.webmvc.context.impl

import java.{ util => ju }
import org.beangle.commons.lang.Locales
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.webmvc.context.LocaleResolver
import java.{util => ju}

class ParamLocaleResolver extends LocaleResolver {
  val SessionAttribute = "WW_TRANS_I18N_LOCALE"
  val SessionParameter = "session_locale"
  val RequestParameter = "request_locale"

  override def resolve(request: HttpServletRequest): ju.Locale = {
    var locale: ju.Locale = request.getAttribute("locale").asInstanceOf[ju.Locale]
    if (null == locale) {
      // get session locale
      var session = request.getSession()
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