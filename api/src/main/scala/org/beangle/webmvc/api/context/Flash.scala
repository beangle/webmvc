/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.api.context

import java.net.{ URLDecoder, URLEncoder }
import java.util.{ Collection, HashMap, Map, Set }

import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.CookieUtils

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object Flash {

  val MessagesKey = "messages"
  val ErrorsKey = "errors"
  private val CookieName = "beangle_flash"
}

@SerialVersionUID(-5292283953338410228L)
class Flash(request: HttpServletRequest, response: HttpServletResponse) extends Serializable {

  /**
   * current request
   */
  val now: Map[String, String] = new HashMap()

  /**
   * next request
   */
  private val next: Map[String, String] = new HashMap()

  readCookieToNow()

  private def readCookieToNow(): Unit = {
    val cookie = CookieUtils.getCookie(request, Flash.CookieName)
    if (null != cookie) {
      val cv = cookie.getValue
      Strings.split(cv, ",") foreach { pair =>
        val key = Strings.substringBefore(pair, "=")
        val v = Strings.substringAfter(pair, "=")
        now.put(key, URLDecoder.decode(v, "utf-8"))
      }
      CookieUtils.deleteCookieByName(request, response, Flash.CookieName)
    }
  }

  private def writeNextToCookie(): Unit = {
    val sb = new StringBuilder
    val i = next.entrySet().iterator()
    while (i.hasNext()) {
      val e = i.next()
      sb.append(e.getKey).append('=').append(URLEncoder.encode(e.getValue, "utf-8")).append(",")
    }
    if (sb.length > 0) {
      sb.deleteCharAt(sb.length - 1)
      CookieUtils.addCookie(request, response, Flash.CookieName, sb.toString(), -1)
    } else {
      CookieUtils.deleteCookieByName(request, response, Flash.CookieName)
    }
  }

  def get(key: Object): String = now.get(key)

  def put(key: String, value: String): String = {
    next.put(key, value)
    writeNextToCookie()
    value
  }

  def putAll(values: Map[_ <: String, _ <: String]): Unit = {
    next.putAll(values)
    writeNextToCookie()
  }

  def keep(key: String): Unit = {
    next.put(key, now.get(key))
    writeNextToCookie()
  }

  def keep() {
    next.putAll(now)
    writeNextToCookie()
  }

  def clear() {
    now.clear()
  }

  import Flash._
  /**
   * 添加消息到下一次请求
   */
  def addMessage(message: String) {
    updateMessages(next, MessagesKey, message)
  }

  /**
   * 添加错误消息到下一次请求
   */
  def addError(error: String) {
    updateMessages(next, ErrorsKey, error)
  }

  /**
   * 添加消息到本次请求
   */
  def addMessageNow(message: String) {
    updateMessages(now, MessagesKey, message)
  }

  /**
   * 添加错误到本次请求
   */
  def addErrorNow(message: String): Unit = {
    updateMessages(now, ErrorsKey, message)
  }

  def messages: List[String] = {
    val m = now.get(MessagesKey)
    if (null == m) List.empty
    else {
      Strings.split(m, ';').toList
    }
  }

  def errors: List[String] = {
    val m = now.get(ErrorsKey)
    if (null == m) List.empty
    else {
      Strings.split(m, ';').toList
    }
  }

  private def updateMessages(map: Map[String, String], key: String, content: String): Unit = {
    val exist = map.get(key)
    map.put(key, if (null == exist) content else (exist + ";" + content))
  }
}
