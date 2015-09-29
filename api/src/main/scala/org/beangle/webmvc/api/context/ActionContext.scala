/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import java.{ util => ju }
import org.beangle.commons.collection.Collections
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.api.i18n.TextProvider

object ActionContextHolder {
  val contexts = new ThreadLocal[ActionContext]
  def context: ActionContext = contexts.get()
}

final class ActionContext(val request: HttpServletRequest, val response: HttpServletResponse, val locale: ju.Locale, val params: Map[String, Any]) {

  var textProvider: Option[TextProvider] = None

  private var flashMap: Flash = _

  private val temp = new collection.mutable.HashMap[String, Any]

  def attribute(name: String, value: Any): Unit = {
    request.setAttribute(name, value)
  }

  def removeAttribute(names: String*) {
    names foreach { name =>
      request.removeAttribute(name)
    }
  }

  def attribute[T](name: String): T = {
    request.getAttribute(name).asInstanceOf[T]
  }

  def temp(name: String, value: Any): Unit = {
    temp.put(name, value)
  }

  def temp[T](name: String): T = {
    temp.get(name).orNull.asInstanceOf[T]
  }

  def flash: Flash = {
    if (null == flashMap) {
      val session = request.getSession()
      val flashObj = session.getAttribute("flash")
      if (null != flashObj) flashMap = flashObj.asInstanceOf[Flash]
      else {
        flashMap = new Flash
        session.setAttribute("flash", flashMap)
      }
    }
    flashMap
  }
}