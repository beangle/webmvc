/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.execution

import jakarta.servlet.http.HttpServletRequest
import org.beangle.cache.{Cache, CacheManager}
import org.beangle.commons.bean.Initializing
import org.beangle.commons.web.url.UrlBuilder

trait ResponseCache{
  def put(request: HttpServletRequest, contentType: String, data: Array[Byte]): Unit

  def get(request: HttpServletRequest): Option[CacheElem]
}

object EmptyResponseCache extends ResponseCache{
  def put(request: HttpServletRequest, contentType: String, data: Array[Byte]): Unit={
  }

  def get(request: HttpServletRequest): Option[CacheElem]={
    None
  }
}

class DefaultResponseCache(cm: CacheManager) extends ResponseCache with Initializing {

  var cache: Cache[String, CacheElem] = _

  override def init(): Unit = {
    cache = cm.getCache("webmvc_handler_cache", classOf[String], classOf[CacheElem])
  }

  override def put(request: HttpServletRequest, contentType: String, data: Array[Byte]): Unit = {
    cache.put(UrlBuilder(request).buildRequestUrl(), CacheElem(contentType, data))
  }

  override def get(request: HttpServletRequest): Option[CacheElem] = {
    cache.get(UrlBuilder(request).buildRequestUrl())
  }
}
