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

import org.beangle.commons.lang.annotation.spi
import org.beangle.web.servlet.util.CookieUtils
import org.beangle.webmvc.annotation.DefaultNone

@spi
trait Argument {

  def name: String

  def value(context: ActionContext): AnyRef

  def required: Boolean

  def defaultValue: Any
}

abstract class AbstractArgument(val name: String, val required: Boolean, val defaultValue: String) extends Argument {
  def handleNone(): AnyRef = {
    if (required) {
      if defaultValue == DefaultNone.value then throw new IllegalArgumentException(s"Cannot bind $name parameter.") else defaultValue
    } else {
      if (defaultValue == DefaultNone.value) null else defaultValue
    }
  }
}

class ParamArgument(name: String, required: Boolean, defaultValue: String) extends AbstractArgument(name, required, defaultValue) {
  override def value(context: ActionContext): AnyRef = {
    context.params.get(name) match {
      case Some(v) => v.asInstanceOf[AnyRef]
      case None => handleNone()
    }
  }

  override def toString: String = {
    "@param(" + name + ")" + (if (required) "*" else "") + (if (defaultValue == DefaultNone.value) "" else " " + defaultValue)
  }
}

class CookieArgument(name: String, required: Boolean, defaultValue: String) extends AbstractArgument(name, required, defaultValue) {

  override def value(context: ActionContext): AnyRef = {
    val cookie = CookieUtils.getCookie(context.request, name)
    if (cookie == null) handleNone()
    else cookie.getValue
  }

  override def toString: String = {
    "@cookie(" + name + ")" + (if (required) "*" else "") + (if (defaultValue == DefaultNone.value) "" else " " + defaultValue)
  }
}

class HeaderArgument(name: String, required: Boolean, defaultValue: String) extends AbstractArgument(name, required, defaultValue) {
  override def value(context: ActionContext): AnyRef = {
    val header = context.request.getHeader(name)
    if (header == null) handleNone() else header
  }

  override def toString: String = {
    "@header(" + name + ")" + (if (required) "*" else "") + (if (defaultValue == DefaultNone.value) "" else " " + defaultValue)
  }
}

object RequestArgument extends Argument {

  override def name = "request"

  override def defaultValue: String = {
    null
  }

  override def value(context: ActionContext): AnyRef = {
    context.request
  }

  override def toString: String = {
    name
  }

  override def required: Boolean = {
    true
  }
}

object ResponseArgument extends Argument {

  override def value(context: ActionContext): AnyRef = {
    context.response
  }

  override def defaultValue: String = {
    null
  }

  override def name = "response"

  override def toString: String = {
    name
  }

  override def required: Boolean = {
    true
  }
}
