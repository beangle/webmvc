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

import java.{ util => ju }
import org.beangle.commons.collection.Collections
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.api.i18n.TextProvider

object ActionContext {
  private val contexts = new ThreadLocal[ActionContext]

  def set(newer: ActionContext): Unit = {
    contexts.set(newer)
  }
  def current: ActionContext = contexts.get()
}

final class ActionContext(val request: HttpServletRequest, val response: HttpServletResponse, val locale: ju.Locale, val params: Map[String, Any]) {

  var textProvider: Option[TextProvider] = None

  val flash = new Flash(request, response)

  private val stash = new collection.mutable.HashMap[String, Any]

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

  def stash(name: String, value: Any): Unit = {
    stash.put(name, value)
  }

  def stash[T](name: String): T = {
    stash.get(name).orNull.asInstanceOf[T]
  }

}
