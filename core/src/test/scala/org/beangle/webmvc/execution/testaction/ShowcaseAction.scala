package org.beangle.webmvc.execution.testaction

import org.beangle.webmvc.api.action.ActionSupport
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.api.annotation.param

class ShowcaseAction extends ActionSupport {

  def index(): String = {
    println("in Showcase index")
    forward()
  }

  def index2(): Unit = {
    println("in Showcase index2")
  }

  def index3(request: HttpServletRequest, response: HttpServletResponse) {
    println("in Showcase index3")
  }

  def index4(request: HttpServletRequest, @param("id") id: Long) {
    println("in Showcase index3")
  }
}