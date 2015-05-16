package org.beangle.webmvc.api.action

import org.beangle.commons.web.util.{CookieUtils, RequestUtils}
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.ContextHolder

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

trait ServletSupport {

  @ignore
  protected final def request: HttpServletRequest = ContextHolder.context.request

  @ignore
  protected final def response: HttpServletResponse = ContextHolder.context.response

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