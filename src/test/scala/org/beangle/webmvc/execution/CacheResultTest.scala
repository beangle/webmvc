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

package org.beangle.webmvc.execution

import jakarta.servlet.http.HttpServletResponse
import org.mockito.Mockito.{mock, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CacheResultTest extends AnyFunSpec, Matchers {

  private def response(headers: Map[String, String]): HttpServletResponse = {
    val res = mock(classOf[HttpServletResponse])
    when(res.getHeaderNames).thenReturn(java.util.Arrays.asList(headers.keys.toList.sorted*))
    headers.foreach { case (name, value) => when(res.getHeader(name)).thenReturn(value) }
    res
  }

  describe("CacheResult.of") {
    it("采集 action 写入的头，供命中缓存时重放") {
      val res = response(Map(
        "Content-Encoding" -> "gzip",
        "Vary" -> "Accept",
        "Content-Length" -> "128",
        "Content-Type" -> "application/x-msgpack",
        "Set-Cookie" -> "URP_SID=1"))
      val result = CacheResult.of(res, "application/x-msgpack", Array[Byte](1, 2))
      result.contentType should be("application/x-msgpack")
      result.headers should be(Map("Content-Encoding" -> "gzip", "Vary" -> "Accept"))
    }
  }
}
