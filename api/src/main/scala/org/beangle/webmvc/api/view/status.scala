package org.beangle.webmvc.api.view

import javax.servlet.http.HttpServletResponse._

object status {

  def apply(code: Int): View = {
    new StatusView(code)
  }
  val not_found = this(SC_NOT_FOUND)

  val not_modified = this(SC_NOT_MODIFIED)

  val bad_request = this(SC_BAD_REQUEST)

  val forbidden = this(SC_FORBIDDEN)
}

class StatusView(val code: Int) extends View 