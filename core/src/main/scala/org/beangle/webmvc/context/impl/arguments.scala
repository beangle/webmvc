package org.beangle.webmvc.context.impl

import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.web.util.CookieUtils
import org.beangle.webmvc.api.annotation.DefaultNone
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.context.Argument

abstract class AbstractArgument(val name: String, val required: Boolean, val defaultValue: String) extends Argument {
  def handleNone(): AnyRef = {
    if (required) {
      if (defaultValue == DefaultNone.value) throw new IllegalArgumentException(s"Cannot  bind parameter to $name")
      else defaultValue
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
  override def value(context: ActionContext): AnyRef = {
    context.request
  }

  override def toString: String = {
    "request"
  }
  override def required: Boolean = {
    true
  }
}

object ResponseArgument extends Argument {
  override def value(context: ActionContext): AnyRef = {
    context.response
  }
  override def toString: String = {
    "response"
  }
  override def required: Boolean = {
    true
  }
}