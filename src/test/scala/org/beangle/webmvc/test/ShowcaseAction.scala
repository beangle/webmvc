/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.test

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.webmvc.annotation.*
import org.beangle.webmvc.support.RouteSupport
import org.beangle.webmvc.view.{Status, View}

class ShowcaseAction extends RouteSupport {

  def string(): View = {
    println("in Showcase string")
    forward()
  }

  def request(request: HttpServletRequest, response: HttpServletResponse): View = {
    require(null != request && null != response)
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
    require(null != accept)
    require(null != request)
    require(id > 0)

    println("in Showcase path")
    Status.Ok
  }

  @response
  @mapping("echofloat/{num}")
  def echofloat(num: Float, request: HttpServletRequest, @header("Accept") accept: String): String = {
    "in Showcase echo :" + num
  }

  @response
  @mapping("echoid")
  def echoid(id: Long): String = {
    "in Showcase echo id:" + id
  }

  @response
  def ok(@param("n", false) n: String): Boolean = {
    true
  }
}
