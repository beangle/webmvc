package org.beangle.webmvc.spring

import java.{ util => ju }

import org.springframework.web.servlet.LocaleResolver

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class BeangleLocaleResolver extends LocaleResolver {

  var innerResolver: org.beangle.webmvc.context.LocaleResolver = _

  override def resolveLocale(request: HttpServletRequest): ju.Locale = {
    innerResolver.resolve(request)
  }

  override def setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: ju.Locale): Unit = {
    innerResolver.setLocale(request, response, locale)
  }
}