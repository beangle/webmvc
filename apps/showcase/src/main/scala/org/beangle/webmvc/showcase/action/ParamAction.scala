package org.beangle.webmvc.showcase.action

import org.beangle.webmvc.api.annotation.cookie
import org.beangle.webmvc.api.annotation.response
import org.springframework.http.HttpRequest
import org.beangle.webmvc.api.annotation.param
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.api.annotation.header

class ParamAction {

  @response
  def cookie(@cookie(value = "JSESSIONID", required = false) sid: String): String = {
    "Cookie JSESSIONID:" + String.valueOf(sid)
  }

  @response
  def param(@param("item") item: String, request: HttpServletRequest, response: HttpServletResponse): String = {
    item
  }

  @response
  def header(@header("Accept") accept: String, request: HttpServletRequest, response: HttpServletResponse): String = {
    accept
  }
}