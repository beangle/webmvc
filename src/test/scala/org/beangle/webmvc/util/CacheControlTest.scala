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

package org.beangle.webmvc.util

import java.time.Duration

import jakarta.servlet.http.HttpServletResponse
import org.mockito.ArgumentMatchers.{anyLong, eq => eqTo}
import org.mockito.Mockito.{mock, verify}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CacheControlTest extends AnyFunSpec, Matchers {

  describe("CacheControl.expiresAfter") {
    it("按 duration 换算 max-age，缺省 private") {
      val res = mock(classOf[HttpServletResponse])
      CacheControl.expiresAfter(Duration.ofMinutes(5), response = res)
      verify(res).setHeader("Cache-Control", "private, max-age=300")
    }

    it("shareable 时使用 public") {
      val res = mock(classOf[HttpServletResponse])
      CacheControl.expiresAfter(Duration.ofDays(4), shareable = true, response = res)
      verify(res).setHeader("Cache-Control", "public, max-age=345600")
    }

    it("同时输出 HTTP/1.0 的 Expires 回退头") {
      val res = mock(classOf[HttpServletResponse])
      CacheControl.expiresAfter(Duration.ofMinutes(5), response = res)
      verify(res).setDateHeader(eqTo("Expires"), anyLong())
    }
  }
}
