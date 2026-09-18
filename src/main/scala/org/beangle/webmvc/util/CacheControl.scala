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

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.webmvc.context.ActionContext

object CacheControl {

  /**
   * 声明当前响应的缓存时长。
   *
   * Cache-Control 是 HTTP/1.1 的权威指令，Expires 只作为 HTTP/1.0 的兼容回退。
   *
   * @param duration  缓存时长，如 Duration.ofMinutes(5)、Duration.ofDays(4)
   * @param shareable true 表示允许 CDN、代理等共享缓存保存（Cache-Control: public），
   *                  适用于 logo、图片等与用户无关的资源；缺省 false（private），
   *                  因为响应往往随用户身份变化，一旦进入共享缓存就会被其他用户命中
   * @param response  响应对象
   */
  def expiresAfter(duration: Duration, shareable: Boolean = false,
                   response: HttpServletResponse = ActionContext.current.response): this.type = {
    val seconds = math.max(0L, duration.toSeconds)
    response.setHeader("Cache-Control", (if (shareable) "public" else "private") + s", max-age=$seconds")
    response.setDateHeader("Expires", System.currentTimeMillis() + seconds * 1000)
    this
  }

  /**
   * 按声明式缓存补齐缓存指令；action 已自行设置 Cache-Control 时整套交给它，不再动 Pragma/Expires。
   *
   * Pragma/Expires 与 Cache-Control 是同一个决策（本来只为禁用缓存时的 HTTP/1.0 兼容），
   * 单独补一个 Pragma: no-cache 会和 action 的 max-age 自相矛盾。
   *
   * @param maxAgeSecond 声明式缓存秒数，<=0 表示禁用缓存
   */
  private[webmvc] def fillIfAbsent(response: HttpServletResponse, maxAgeSecond: Int): Unit = {
    if (null == response.getHeader("Cache-Control")) {
      if (maxAgeSecond <= 0) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private")
        response.setHeader("Pragma", "no-cache") // 兼容 HTTP/1.0
        response.setHeader("Expires", "0") // 兼容 HTTP/1.0
      } else {
        response.setHeader("Cache-Control", s"public,s-maxage=$maxAgeSecond")
      }
    }
  }

  /**
   * return true if already has it's etag
   */
  def withEtag(etag: String, request: HttpServletRequest = ActionContext.current.request,
               response: HttpServletResponse = ActionContext.current.response): Boolean = {
    val requestETag = request.getHeader("If-None-Match")
    response.setHeader("ETag", etag)

    // not modified, content is not sent - only basic headers and status SC_NOT_MODIFIED
    if (etag.equals(requestETag)) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
      true
    } else false
  }
}
