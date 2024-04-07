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

package org.beangle.webmvc.execution.interceptors

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.annotation.description
import org.beangle.web.servlet.intercept.Interceptor

object CorsInterceptor {
  // Request headers
  val OriginHeader = "Origin"
  val RequestMethodHeader = "Access-Control-Request-Method"
  val RequestHeadersHeader = "Access-Control-Request-Headers"
  // Response headers
  val AllowOriginHeader = "Access-Control-Allow-Origin"
  val AllowMethodsHeader = "Access-Control-Allow-Methods"
  val AllowHeadersHeader = "Access-Control-Allow-Headers"
  val MaxAgeHeader = "Access-Control-Max-Age"
  val AllowCredentialsHeader = "Access-Control-Allow-Credentials"
  val ExposeHeadersHeader = "Access-Control-Expose-Headers"

  val AnyOrigin = "*"
  val ComplexHttpMethods: Set[String] = Set("PUT", "DELETE", "TRACE", "CONNECT")
  val SimpleHttpContentTypes: Set[String] = Set("application/x-www-form-urlencoded", "multipart/form-data", "text/plain")
}

object CORSRequestType {
  val SIMPLE = 1
  val ACTUAL = 2
  val PRE_FLIGHT = 3
  val INVALID_CORS = 9
}

@description("支持跨域调用CORS的拦截器")
class CorsInterceptor extends Interceptor with Initializing {

  import CORSRequestType._
  import CorsInterceptor._

  var anyOriginAllowed: Boolean = _
  var allowedOrigins: Set[String] = Set(AnyOrigin)
  var allowedMethods: Set[String] = Set("GET", "POST", "HEAD", "OPTIONS")
  var allowedHeaders: Set[String] = Set("X-Requested-With", "Content-Type", "Accept", "Origin")
  var exposedHeaders: Set[String] = Set.empty[String]
  var preflightMaxAge: Int = 1800 //30min
  var allowCredentials = false
  var chainPreflight = true

  def init(): Unit = {
    anyOriginAllowed = allowedOrigins.contains(AnyOrigin)
  }

  def preInvoke(req: HttpServletRequest, res: HttpServletResponse): Boolean = {
    val origin = req.getHeader(OriginHeader)
    if (origin == null) return true
    checkRequestType(origin, req) match {
      case SIMPLE | ACTUAL => handleSimpleCors(req, res, origin)
      case PRE_FLIGHT => handlePreflightCors(req, res, origin)
      case INVALID_CORS => handleInvalidCORS(res)
    }
  }

  def postInvoke(req: HttpServletRequest, res: HttpServletResponse): Unit = {
  }

  private def handleInvalidCORS(res: HttpServletResponse): Boolean = {
    res.setContentType("text/plain")
    res.setStatus(HttpServletResponse.SC_FORBIDDEN)
    res.resetBuffer()
    false
  }

  private def handleSimpleCors(req: HttpServletRequest, res: HttpServletResponse, origin: String): Boolean = {
    if (anyOriginAllowed && !allowCredentials) res.addHeader(AllowOriginHeader, AnyOrigin)
    else res.addHeader(AllowOriginHeader, origin)
    if (allowCredentials) res.setHeader(AllowCredentialsHeader, "true")
    if (exposedHeaders.nonEmpty) res.setHeader(ExposeHeadersHeader, exposedHeaders.mkString(","))
    true
  }

  private def handlePreflightCors(req: HttpServletRequest, res: HttpServletResponse, origin: String): Boolean = {
    if (!allowedMethods.contains(req.getHeader(RequestMethodHeader))) return false
    val headersAllowed = areHeadersAllowed(req)
    if (!headersAllowed) return false
    res.setHeader(AllowOriginHeader, origin)
    if (allowCredentials) res.setHeader(AllowCredentialsHeader, "true")
    if (preflightMaxAge > 0) res.setHeader(MaxAgeHeader, String.valueOf(preflightMaxAge))
    res.setHeader(AllowMethodsHeader, allowedMethods.mkString(","))
    res.setHeader(AllowHeadersHeader, allowedHeaders.mkString(","))
    chainPreflight
  }

  private def areHeadersAllowed(req: HttpServletRequest): Boolean = {
    val accessControlRequestHeaders = req.getHeader(RequestHeadersHeader)
    (accessControlRequestHeaders == null) || accessControlRequestHeaders.split(",").toSet.subsetOf(allowedHeaders)
  }

  private def checkRequestType(origin: String, req: HttpServletRequest): Int = {
    if (isOriginAllowed(origin)) {
      val method = req.getMethod
      if (allowedMethods.contains(method)) {
        if ("OPTIONS".equals(method)) {
          val methodHeader = req.getHeader(RequestMethodHeader)
          if (allowedMethods.contains(methodHeader)) PRE_FLIGHT else INVALID_CORS
        } else if ("GET" == method || "HEAD" == method) {
          SIMPLE
        } else if ("POST" == method) {
          val contentType = req.getContentType
          if (contentType != null) if (SimpleHttpContentTypes.contains(contentType.toLowerCase.trim)) SIMPLE else ACTUAL
          else INVALID_CORS
        } else if (ComplexHttpMethods.contains(method)) {
          ACTUAL
        } else {
          INVALID_CORS
        }
      } else INVALID_CORS
    } else INVALID_CORS
  }

  private def isOriginAllowed(origin: String): Boolean = {
    if (anyOriginAllowed) origin.indexOf('%') == -1
    else allowedOrigins.contains(origin)
  }
}
