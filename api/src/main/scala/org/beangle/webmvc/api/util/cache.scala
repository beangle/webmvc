package org.beangle.webmvc.api.util

import org.beangle.webmvc.api.context.ContextHolder
import java.{ util => ju }

object CacheControl {

  def expiresAfter(days: Int): this.type = {
    val response = ContextHolder.context.response
    val cal = ju.Calendar.getInstance()
    cal.add(ju.Calendar.DAY_OF_MONTH, days)
    val expires = cal.getTimeInMillis()
    response.setDateHeader("Date", System.currentTimeMillis())
    response.setDateHeader("Expires", expires)
    response.setDateHeader("Retry-After", expires)
    response.setHeader("Cache-Control", "public")
    this
  }
}