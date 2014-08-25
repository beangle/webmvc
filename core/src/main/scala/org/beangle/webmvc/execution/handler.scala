package org.beangle.webmvc.execution

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.ActionMapping

trait Handler {
  def action: AnyRef
  def handle(mapping: ActionMapping): Any
}

trait Interceptor {

  def preHandle(handler: Handler): Boolean

  def postHandle(handler: Handler, result: Any): Unit
}
