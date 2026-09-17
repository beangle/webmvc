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
import org.beangle.commons.lang.annotation.description
import org.beangle.web.servlet.intercept.Interceptor
import org.beangle.web.servlet.util.RequestUtils

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

  val ComplexHttpMethods: Set[String] = Set("PUT", "DELETE", "TRACE", "CONNECT")

  /** 判断是否为跨域请求。
   *
   * 与浏览器同源判定保持一致，比较 **scheme + host + port** 三者（与凭据如何传递无关）：
   * 端口不同（如页面 `http://localhost:5173` 调用后端 `http://localhost`）浏览器视为跨域，
   * 这里就必须按跨域处理，否则不会下发 `Access-Control-Allow-Origin`，浏览器只会报
   * `CORS missing Allow Origin`。
   *
   * 做法：把请求自身还原成 origin（默认端口省略，见 `RequestUtils.getOrigin`），直接与 `Origin` 头比较。
   * 浏览器发出的 `Origin` 一定带 scheme 且遵守默认端口省略规则，所以字符串比较既简单又准确。
   * 反代下若只透传 `Host` 而不透传 `X-Forwarded-Proto/Port`，仍可能误判，需要代理补齐转发头。
   */
  def isCorsRequest(req: HttpServletRequest): Boolean = {
    val origin = req.getHeader(OriginHeader)
    if (null == origin) false
    else !RequestUtils.getOrigin(req).equalsIgnoreCase(origin.trim)
  }
}

object CORSRequestType {
  val SIMPLE = 1
  val ACTUAL = 2
  val PRE_FLIGHT = 3
  val INVALID_CORS = 9
}

@description("支持跨域调用CORS的拦截器")
class CorsInterceptor extends Interceptor {

  import CORSRequestType.*
  import CorsInterceptor.*

  /** Allowed values: full origins (`http://a.com`), or hosts (`localhost`, `127.0.0.1`). */
  var allowedOrigins: Set[String] = Set("localhost", "127.0.0.1")
  var allowedMethods: Set[String] = Set("GET", "POST", "HEAD", "OPTIONS", "PATCH")
  var allowedHeaders: Set[String] = Set("x-requested-with", "content-type", "accept", "origin")
  var exposedHeaders: Set[String] = Set.empty[String]
  var preflightMaxAge: Int = 1800 //30min
  var allowCredentials = true
  var chainPreflight = false

  def preInvoke(req: HttpServletRequest, res: HttpServletResponse): Boolean = {
    if (CorsInterceptor.isCorsRequest(req)) {
      val origin = req.getHeader(OriginHeader)
      checkRequestType(origin, req) match {
        case SIMPLE | ACTUAL => handleSimpleCors(req, res, origin)
        case PRE_FLIGHT => handlePreflightCors(req, res, origin)
        case INVALID_CORS => handleInvalidCORS(res)
      }
    } else {
      true
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
    res.addHeader(AllowOriginHeader, origin)
    if (allowCredentials) {
      res.setHeader(AllowCredentialsHeader, "true")
      res.setHeader("Vary", "Origin")
    }
    if (exposedHeaders.nonEmpty) res.setHeader(ExposeHeadersHeader, exposedHeaders.mkString(","))
    true
  }

  private def handlePreflightCors(req: HttpServletRequest, res: HttpServletResponse, origin: String): Boolean = {
    val headersAllowed = areHeadersAllowed(req)
    if (!headersAllowed) return false
    res.setHeader(AllowOriginHeader, origin)
    if (allowCredentials) res.setHeader(AllowCredentialsHeader, "true")
    if (preflightMaxAge > 0) res.setHeader(MaxAgeHeader, String.valueOf(preflightMaxAge))
    res.setHeader(AllowMethodsHeader, allowedMethods.mkString(","))
    res.setHeader(AllowHeadersHeader, allowedHeaders.mkString(","))
    res.setStatus(204)
    chainPreflight
  }

  private def areHeadersAllowed(req: HttpServletRequest): Boolean = {
    val accessControlRequestHeaders = req.getHeader(RequestHeadersHeader)
    (accessControlRequestHeaders == null) || accessControlRequestHeaders.toLowerCase().split(",").toSet.subsetOf(allowedHeaders)
  }

  private def checkRequestType(origin: String, req: HttpServletRequest): Int = {
    if (isOriginAllowed(origin)) {
      val method = getRequestMethod(req)
      if (allowedMethods.contains(method)) {
        if ("OPTIONS".equals(method)) {
          val methodHeader = getRequestMethodHeader(req)
          if (allowedMethods.contains(methodHeader)) PRE_FLIGHT else INVALID_CORS
        } else if ("GET" == method || "HEAD" == method) {
          SIMPLE
        } else if ("POST" == method || "PATCH" == method) {
          val contentType = req.getContentType
          if (contentType != null) ACTUAL else INVALID_CORS
        } else if (ComplexHttpMethods.contains(method)) {
          ACTUAL
        } else {
          INVALID_CORS
        }
      } else INVALID_CORS
    } else INVALID_CORS
  }

  private def isOriginAllowed(origin: String): Boolean = {
    if (origin.indexOf('%') != -1) false
    else if (allowedOrigins.contains(origin)) true
    else extractHost(origin).exists(host => allowedOrigins.contains(host))
  }

  private def extractHost(origin: String): Option[String] = {
    try {
      Option(java.net.URI.create(origin).getHost)
    } catch {
      case _: Exception => None
    }
  }

  private def getRequestMethod(req: HttpServletRequest): String = {
    req.getMethod.toUpperCase()
  }

  private def getRequestMethodHeader(req: HttpServletRequest): String = {
    req.getHeader(RequestMethodHeader).toUpperCase()
  }
}
