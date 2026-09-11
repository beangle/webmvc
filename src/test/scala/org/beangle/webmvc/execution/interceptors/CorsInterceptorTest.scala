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
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CorsInterceptorTest extends AnyFunSpec, Matchers {

  private val interceptor = new CorsInterceptor

  private def request(origin: String, host: String, method: String = "POST",
                      serverName: String = "learning.example.edu.cn",
                      contentType: String = "application/x-www-form-urlencoded"): HttpServletRequest = {
    val req = mock(classOf[HttpServletRequest])
    when(req.getHeader(CorsInterceptor.OriginHeader)).thenReturn(origin)
    when(req.getHeader("Host")).thenReturn(host)
    when(req.getMethod).thenReturn(method)
    when(req.getServerName).thenReturn(serverName)
    when(req.getContentType).thenReturn(contentType)
    req
  }

  private def response(): HttpServletResponse = mock(classOf[HttpServletResponse])

  describe("isCorsRequest") {
    it("无 Origin 头（如地址栏 GET）不是跨域请求") {
      CorsInterceptor.isCorsRequest(request(null, null)) should be(false)
    }

    it("同源 POST（浏览器也会带 Origin）不是跨域请求") {
      CorsInterceptor.isCorsRequest(
        request("https://learning.example.edu.cn", "learning.example.edu.cn")) should be(false)
    }

    it("同源带非默认端口时，端口一致则不是跨域请求") {
      CorsInterceptor.isCorsRequest(
        request("http://learning.example.edu.cn:8080", "learning.example.edu.cn:8080")) should be(false)
    }

    it("同主机但端口不同不是跨域请求（只比较主机名）") {
      CorsInterceptor.isCorsRequest(
        request("http://localhost:5173", "localhost", serverName = "localhost")) should be(false)
    }

    it("同主机但协议不同不是跨域请求（只比较主机名）") {
      CorsInterceptor.isCorsRequest(
        request("https://learning.example.edu.cn", "learning.example.edu.cn:8080")) should be(false)
    }

    it("反向代理改写 Host 头时，以 getServerName 为准，同源请求不是跨域") {
      CorsInterceptor.isCorsRequest(
        request("https://learning.example.edu.cn", "10.0.0.5:8080")) should be(false)
    }

    it("不同主机是跨域请求") {
      CorsInterceptor.isCorsRequest(
        request("https://evil.example.com", "learning.example.edu.cn")) should be(true)
    }

    it("前缀相同但主机名被追加的伪装 Origin 是跨域请求") {
      CorsInterceptor.isCorsRequest(
        request("https://learning.example.edu.cn.evil.com", "learning.example.edu.cn")) should be(true)
    }

    it("主机名含下划线时仍能识别为同源") {
      CorsInterceptor.isCorsRequest(
        request("http://foo_bar.example.edu.cn:8080", "foo_bar.example.edu.cn",
          serverName = "foo_bar.example.edu.cn")) should be(false)
    }

    it("缺少 scheme 的 Origin 也能识别为同源") {
      CorsInterceptor.isCorsRequest(
        request("learning.example.edu.cn:8080", "learning.example.edu.cn",
          serverName = "learning.example.edu.cn")) should be(false)
    }

    it("无法解析的 Origin 视为跨域请求") {
      CorsInterceptor.isCorsRequest(request("http://bad host", "learning.example.edu.cn")) should be(true)
    }
  }

  describe("CorsInterceptor") {
    it("无 Origin 头的请求直接放行") {
      interceptor.preInvoke(request(null, null), response()) should be(true)
    }

    it("同源 POST（浏览器也会带 Origin）应放行") {
      val req = request("https://learning.example.edu.cn", "learning.example.edu.cn")
      interceptor.preInvoke(req, response()) should be(true)
    }

    it("同主机不同端口的请求直接放行（不再进入白名单校验）") {
      interceptor.preInvoke(
        request("http://localhost:5173", "localhost", serverName = "localhost"), response()) should be(true)
    }

    it("未知来源的跨域 POST 返回 403") {
      val res = response()
      interceptor.preInvoke(request("https://evil.example.com", "learning.example.edu.cn"), res) should be(false)
      verify(res).setStatus(HttpServletResponse.SC_FORBIDDEN)
    }
  }
}
