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
import org.beangle.commons.activation.MediaType
import org.beangle.commons.text.i18n.TextResource
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.context.ActionContext.*
import org.beangle.webmvc.execution.{Handler, MappingHandler}

import java.util as ju
import scala.collection.mutable
import scala.reflect.ClassTag

object ActionContext {
  private val contexts = new ThreadLocal[ActionContext]

  def set(newer: ActionContext): ActionContext = {
    contexts.set(newer)
    newer
  }

  def current: ActionContext = contexts.get()

  val LocalKey = "_beangle_web_local"
  val FlashKey = "_beangle_web_flash"
  val TextResourceKey = "_beangle_web_text_resource"
  val AcceptTypeKey = "_beangle_web_accept_type"
}

final class ActionContext(val request: HttpServletRequest, val response: HttpServletResponse,
                          val handler: Handler, val params: collection.Map[String, Any],
                          private val properties: List[ActionContextProperty]) {

  private val stash = new mutable.HashMap[String, Any]

  def attribute(name: String, value: Any): Unit = {
    request.setAttribute(name, value)
  }

  def removeAttribute(names: String*): Unit = {
    names foreach { name =>
      request.removeAttribute(name)
    }
  }

  def attribute[T](name: String): T = {
    request.getAttribute(name).asInstanceOf[T]
  }

  def stash[T](name: String, defaultValue: T): T = {
    if (null == defaultValue) {
      stash.get(name).orNull.asInstanceOf[T]
    } else {
      stash.getOrElseUpdate(name, defaultValue).asInstanceOf[T]
    }
  }

  def locale: ju.Locale = {
    getProperty[ju.Locale](LocalKey).getOrElse(request.getLocale)
  }

  def textResource: TextResource = {
    getProperty[TextResource](TextResourceKey).getOrElse(TextResource.Empty)
  }

  def acceptTypes: Seq[MediaType] = {
    getProperty[Seq[MediaType]](AcceptTypeKey).getOrElse(Seq.empty)
  }

  def getFlash(createWhenMissing: Boolean): Flash = {
    stash.get(FlashKey) match {
      case Some(f) => f.asInstanceOf[Flash]
      case None =>
        if createWhenMissing then
          val f = new Flash(request, response)
          stash.put(FlashKey, f)
          f
        else null
    }
  }

  def clearFlash(): Unit = {
    stash.remove(FlashKey)
  }

  def mapping: RouteMapping = {
    handler.asInstanceOf[MappingHandler].mapping
  }

  /** Find property and store it into stash.
   *
   * @param name property name
   * @return
   */
  def getProperty[T: ClassTag](name: String): Option[T] = {
    stash.get(name) match {
      case s@Some(v) => s.asInstanceOf[Option[T]]
      case None =>
        properties.find(_.name == name).map(_.get(this)) match {
          case None => None
          case ns@Some(nv) =>
            stash.put(name, nv)
            ns.asInstanceOf[Option[T]]
        }
    }
  }
}
