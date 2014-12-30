package org.beangle.webmvc.api.view

import javax.servlet.http.HttpServletResponse.{ SC_BAD_REQUEST, SC_FORBIDDEN, SC_NOT_FOUND, SC_NOT_MODIFIED, SC_OK }

object Status {

  def apply(code: Int): View = {
    new StatusView(code)
  }
  val Ok = this(SC_OK)

  val NotFound = this(SC_NOT_FOUND)

  val NotModified = this(SC_NOT_MODIFIED)

  val BadRequest = this(SC_BAD_REQUEST)

  val Forbidden = this(SC_FORBIDDEN)
}

class StatusView(val code: Int) extends View 