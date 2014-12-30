package org.beangle.webmvc.execution.testaction

import org.beangle.webmvc.api.action.ActionSupport
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.api.annotation.param
import org.beangle.webmvc.api.annotation.cookie
import org.beangle.webmvc.api.annotation.header
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.annotation.response
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.api.view.Status

class ShowcaseAction extends ActionSupport {

  def string(): String = {
    println("in Showcase string")
    forward()
  }

  def request(request: HttpServletRequest, response: HttpServletResponse): View = {
    println("in Showcase request")
    Status.Ok
  }

  def param(@param(value = "id", required = false, defaultValue = "3") id: java.lang.Long): View = {
    println("in Showcase param")
    Status.Ok
  }

  def cookie(@cookie("JSESSIONID") id: String): View = {
    println("in Showcase cookie")
    Status.Ok
  }

  def header(@header("Accept") id: String): View = {
    println("in Showcase header")
    Status.Ok
  }

  @mapping("path/{id}")
  def path(id: Int, request: HttpServletRequest, @header("Accept") accept: String): View = {
    println("in Showcase path")
    Status.Ok
  }

  @response
  @mapping("echofloat/{num}")
  def echofloat(num: Float, request: HttpServletRequest, @header("Accept") accept: String): String = {
    "in Showcase echo :" + num
  }
}