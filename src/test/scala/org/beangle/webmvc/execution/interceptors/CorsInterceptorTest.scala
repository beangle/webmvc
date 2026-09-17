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
import org.mockito.Mockito.{mock, never, verify, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CorsInterceptorTest extends AnyFunSpec, Matchers {

  private val interceptor = new CorsInterceptor

  private def request(origin: String, host: String, method: String = "POST",
                      serverName: String = "learning.example.edu.cn",
                      contentType: String = "application/x-www-form-urlencoded",
                      scheme: String = "https",
                      serverPort: Int = 443,
                      headers: Map[String, String] = Map.empty): HttpServletRequest = {
    val req = mock(classOf[HttpServletRequest])
    when(req.getHeader(CorsInterceptor.OriginHeader)).thenReturn(origin)
    when(req.getHeader("Host")).thenReturn(host)
    when(req.getMethod).thenReturn(method)
    when(req.getServerName).thenReturn(serverName)
    when(req.getContentType).thenReturn(contentType)
    when(req.getScheme).thenReturn(scheme)
    when(req.getServerPort).thenReturn(serverPort)
    when(req.getContextPath).thenReturn("")
    when(req.getRequestURI).thenReturn("/api/edu/learning/configs.json")
    when(req.getQueryString).thenReturn(null)
    headers.foreach { case (name, value) => when(req.getHeader(name)).thenReturn(value) }
    req
  }

  private def response(): HttpServletResponse = mock(classOf[HttpServletResponse])

  describe("CorsInterceptor") {
    it("无 Origin 头的请求直接放行") {
      interceptor.preInvoke(request(null, null), response()) should be(true)
    }

    it("同源 POST（浏览器也会带 Origin）应放行") {
      val req = request("https://learning.example.edu.cn", "learning.example.edu.cn")
      interceptor.preInvoke(req, response()) should be(true)
    }

    it("同主机不同端口按跨域处理，命中白名单时下发允许头") {
      val res = response()
      val req = request("http://localhost:5173", "localhost", serverName = "localhost",
        scheme = "http", serverPort = 80)
      interceptor.preInvoke(req, res) should be(true)
      verify(res).addHeader(CorsInterceptor.AllowOriginHeader, "http://localhost:5173")
      verify(res).setHeader(CorsInterceptor.AllowCredentialsHeader, "true")
    }

    it("同主机不同端口且来源不在白名单时返回 403") {
      val res = response()
      val req = request("http://evil-host:5173", "localhost", serverName = "localhost",
        scheme = "http", serverPort = 80)
      interceptor.preInvoke(req, res) should be(false)
      verify(res).setStatus(HttpServletResponse.SC_FORBIDDEN)
    }

    it("同源请求不附加任何 CORS 头") {
      val res = response()
      interceptor.preInvoke(
        request("https://learning.example.edu.cn", "learning.example.edu.cn"), res) should be(true)
      verify(res, never()).addHeader(CorsInterceptor.AllowOriginHeader, "https://learning.example.edu.cn")
    }

    it("同主机同端口时同样不附加 CORS 头") {
      val res = response()
      interceptor.preInvoke(
        request("http://learning.example.edu.cn", "learning.example.edu.cn",
          scheme = "http", serverPort = 80), res) should be(true)
      verify(res, never()).addHeader(CorsInterceptor.AllowOriginHeader, "http://learning.example.edu.cn")
    }

    it("未知来源的跨域 POST 返回 403") {
      val res = response()
      interceptor.preInvoke(request("https://evil.example.com", "learning.example.edu.cn"), res) should be(false)
      verify(res).setStatus(HttpServletResponse.SC_FORBIDDEN)
    }
  }
}
