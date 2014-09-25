package org.beangle.webmvc.api.util

import javax.servlet.http.HttpServletResponse
import org.beangle.webmvc.api.context.ContextHolder

/**
 * Cross Origin Resource Sharing
 * @see http://www.w3.org/TR/cors/
 */
object CORS {

  def allow(origins: String): this.type = {
    allow(origins, null, null)
  }

  def allow(origins: String, methods: String, headers: String): this.type = {
    val response = ContextHolder.context.response
    response.addHeader("Access-Control-Allow-Origin", origins)
    if (null != methods) response.addHeader("Access-Control-Allow-Methods", methods)
    if (null != headers) response.addHeader("Access-Control-Allow-Headers", headers)
    this
  }

  def expose(header: String): this.type = {
    val response = ContextHolder.context.response
    response.addHeader("Access-Control-Expose-Headers", header)
    this
  }

  def maxage(age: Int): this.type = {
    val response = ContextHolder.context.response
    response.addIntHeader("Access-Control-Max-Age", age)
    this
  }
}