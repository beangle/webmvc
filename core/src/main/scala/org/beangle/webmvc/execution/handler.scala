package org.beangle.webmvc.execution

import org.beangle.webmvc.dispatch.ActionMapping
import org.beangle.commons.lang.annotation.spi

trait Handler {
  def action: AnyRef
  def handle(mapping: ActionMapping): Any
}

trait Interceptor {

  def preHandle(handler: Handler): Boolean

  def postHandle(handler: Handler, result: Any): Unit
}

@spi
trait InvocationReactor{
  def invoke(handler:Handler,mapping: ActionMapping):Unit
}