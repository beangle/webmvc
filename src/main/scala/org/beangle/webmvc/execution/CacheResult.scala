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

import java.util.Locale

import jakarta.servlet.http.HttpServletResponse

/**
 * 可缓存的响应：内容类型、action 自行写入的响应头以及响应体字节。
 *
 * @param contentType 内容类型
 * @param headers     action 写入的响应头，命中缓存时需要原样重放
 * @param data        响应体字节
 */
case class CacheResult(contentType: String, headers: Map[String, String], data: Array[Byte])

object CacheResult {

  /** 由容器管理、或不该从缓存重放的头 */
  private val Excluded = Set(
    "content-type", "content-length", "date", "transfer-encoding",
    "connection", "keep-alive", "set-cookie")

  /** 采集 action 已写入当前响应的头，供命中缓存时重放。 */
  def of(response: HttpServletResponse, contentType: String, data: Array[Byte]): CacheResult = {
    val headers = new collection.mutable.LinkedHashMap[String, String]
    val names = response.getHeaderNames
    if (null != names) {
      val iter = names.iterator
      while (iter.hasNext) {
        val name = iter.next()
        if (!Excluded.contains(name.toLowerCase(Locale.ROOT))) headers.put(name, response.getHeader(name))
      }
    }
    CacheResult(contentType, headers.toMap, data)
  }
}
