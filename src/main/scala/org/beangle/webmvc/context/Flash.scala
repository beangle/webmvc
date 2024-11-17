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

package org.beangle.webmvc.context

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.lang.Strings
import org.beangle.web.servlet.util.CookieUtils

import java.util as ju
import scala.collection.mutable

object Flash {
  private val CookieName = "beangle_flash"
}

@SerialVersionUID(-5292283953338410228L)
class Flash(request: HttpServletRequest, response: HttpServletResponse) extends Serializable {

  /**
   * current request
   */
  private val now = new mutable.HashMap[String, String]

  /**
   * next request
   */
  private val next = new mutable.HashMap[String, String]

  moveCookieToNow()

  private def moveCookieToNow(): Unit = {
    val cv = CookieUtils.getCookieValue(request, Flash.CookieName)
    if (null != cv) {
      Strings.split(cv, ",") foreach { pair =>
        val key = Strings.substringBefore(pair, "=")
        val v = Strings.substringAfter(pair, "=")
        now.put(key, v)
      }
      CookieUtils.deleteCookieByName(request, response, Flash.CookieName)
    }
  }

  def writeNextToCookie(): Unit = {
    if (next.isEmpty) return
    val sb = new StringBuilder
    val i = next.iterator
    while (i.hasNext) {
      val e = i.next()
      val kv = e._1 + "=" + e._2
      sb.append(kv).append(",")
    }
    if (sb.nonEmpty) {
      sb.deleteCharAt(sb.length - 1)
      CookieUtils.addCookie(request, response, Flash.CookieName, sb.toString(), 1)
    } else {
      CookieUtils.deleteCookieByName(request, response, Flash.CookieName)
    }
  }

  def get(key: String): Option[String] = now.get(key)

  def put(key: String, value: String): String = {
    next.put(key, value)
    value
  }

  def putAll(values: collection.Map[String, String]): Unit = {
    next.addAll(values)
  }

  def keep(key: String): Unit = {
    now.get(key) foreach (v => next.put(key, v))
  }

  def keep(): Unit = {
    next.addAll(now)
  }

  def clear(): Unit = {
    now.clear()
  }

  def append(key: String, content: String): Unit = {
    next.get(key) match {
      case Some(c) => next.put(key, c + ";" + content)
      case None => next.put(key, content)
    }
  }

  def appendNow(key: String, content: String): Unit = {
    now.get(key) match {
      case Some(c) => now.put(key, c + ";" + content)
      case None => now.put(key, content)
    }
  }

}
