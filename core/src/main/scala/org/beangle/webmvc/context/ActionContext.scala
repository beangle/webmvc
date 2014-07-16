package org.beangle.webmvc.context

import java.{util => ju}

import org.beangle.commons.lang.Locales
import org.beangle.commons.text.i18n.TextResource

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

object ContextHolder {
  val SessionAttribute = "WW_TRANS_I18N_LOCALE"
  val SessionParameter = "session_locale"
  val RequestParameter = "request_locale"

  val contexts = new ThreadLocal[ActionContext]
  def context: ActionContext = contexts.get()

  protected[context] def extractLocale(request: HttpServletRequest): ju.Locale = {
    var locale: ju.Locale = null
    // get session locale
    var session = request.getSession()
    if (null != session) {
      var session_locale = findLocaleParameter(request, SessionParameter)
      if (null == session_locale) {
        locale = session.getAttribute(SessionAttribute).asInstanceOf[ju.Locale]
      } else {
        locale = Locales.toLocale(session_locale)
        // save it in session
        session.setAttribute(SessionAttribute, locale)
      }
    }
    // get request locale
    var request_locale = findLocaleParameter(request, RequestParameter)
    if (null != request_locale) locale = Locales.toLocale(request_locale)

    if (null != locale) locale = request.getLocale()
    locale
  }

  private def findLocaleParameter(request: HttpServletRequest, parameterName: String): String = {
    var localParam = request.getParameter(parameterName)
    if (null == localParam) null else localParam.toString()
  }
}

class ActionContext(val request: HttpServletRequest, val response: HttpServletResponse, val params: Map[String, Any]) {

  var local = ContextHolder.extractLocale(request)

  def apply(name: String): Any = request.getAttribute(name)

  def put(name: String, value: Any): Unit = {
    request.setAttribute(name, value)
  }

  def flash: Flash = {
    val flash = request.getSession().getAttribute("flash").asInstanceOf[Flash]
    if (null == flash) {
      val nflash = new Flash()
      request.getSession().setAttribute("flash", nflash)
      nflash
    } else flash
  }

  def textResource: TextResource = {
    request.getAttribute("textResource").asInstanceOf[TextResource]
  }
}

