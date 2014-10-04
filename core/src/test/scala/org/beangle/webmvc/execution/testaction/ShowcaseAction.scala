package org.beangle.webmvc.execution.testaction

import org.beangle.webmvc.api.action.ActionSupport
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.api.annotation.param
import org.beangle.webmvc.api.annotation.cookie
import org.beangle.webmvc.api.annotation.header
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.annotation.response

class ShowcaseAction extends ActionSupport {

  def string(): String = {
    println("in Showcase string")
    forward()
  }

  def unit(): Unit = {
    println("in Showcase void")
  }

  def request(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    println("in Showcase request")
  }

  def param(@param(value = "id", required = false, defaultValue = "3") id: java.lang.Long): Unit = {
    println("in Showcase param")
  }

  def cookie(@cookie("JSESSIONID") id: String): Unit = {
    println("in Showcase cookie")
  }

  def header(@header("Accept") id: String): Unit = {
    println("in Showcase header")
  }

  @mapping("path/{id}")
  def path(id: Int, request: HttpServletRequest, @header("Accept") accept: String): Unit = {
    println("in Showcase path")
  }

  @response
  @mapping("echofloat/{num}")
  def echofloat(num: Float, request: HttpServletRequest, @header("Accept") accept: String): String = {
    "in Showcase echo :" + num
  }
}