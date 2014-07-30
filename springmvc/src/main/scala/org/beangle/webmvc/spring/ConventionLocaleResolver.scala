package org.beangle.webmvc.spring

import org.springframework.web.servlet.LocaleResolver
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import java.{ util => ju }
import org.beangle.webmvc.context.ParamLocaleResolver
import java.util.Locale

class ConventionLocaleResolver extends LocaleResolver {

  override def resolveLocale(request: HttpServletRequest): ju.Locale = {
    ParamLocaleResolver.resolve(request)
  }

  override def setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: ju.Locale): Unit = {
    val session = request.getSession(false)
    if (null != session) {
      session.setAttribute(ParamLocaleResolver.SessionAttribute, locale)
    }
  }
}