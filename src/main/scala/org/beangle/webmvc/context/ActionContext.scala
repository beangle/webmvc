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
import org.beangle.commons.text.i18n.TextResource
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.context.ActionContext.{FlashKey, LocalKey, TextResourceKey}
import org.beangle.webmvc.execution.{Handler, MappingHandler}

import java.util as ju
import scala.collection.mutable

object ActionContext {
  private val contexts = new ThreadLocal[ActionContext]

  def set(newer: ActionContext): Unit = {
    contexts.set(newer)
  }

  def current: ActionContext = contexts.get()

  private val LocalKey = "_beangle_web_local"
  private val FlashKey = "_beangle_web_flash"
  private val TextResourceKey = "_beangle_web_text_resource"
}

final class ActionContext(val request: HttpServletRequest, val response: HttpServletResponse, val handler: Handler, val params: collection.Map[String, Any]) {

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

  def stash(name: String, value: Any): Unit = {
    stash.put(name, value)
  }

  def stash[T](name: String): T = {
    stash.get(name).orNull.asInstanceOf[T]
  }

  def locale: ju.Locale = {
    stash.get(LocalKey) match {
      case Some(l) => l.asInstanceOf[ju.Locale]
      case None => request.getLocale
    }
  }

  def locale_=(locale: ju.Locale): Unit = {
    stash.put(LocalKey, locale)
  }

  def textResource: TextResource = {
    stash.get(TextResourceKey) match {
      case Some(l) => l.asInstanceOf[TextResource]
      case None => TextResource.Empty
    }
  }

  def textResource_=(textResource: TextResource): Unit = {
    stash.put(TextResourceKey, textResource)
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
}
