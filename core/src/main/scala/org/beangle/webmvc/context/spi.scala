package org.beangle.webmvc.context

import java.{ util => ju }
import org.beangle.commons.lang.Locales
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

trait LocaleResolver {
  def resolve(request: HttpServletRequest): ju.Locale
  def setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: ju.Locale): Unit
}