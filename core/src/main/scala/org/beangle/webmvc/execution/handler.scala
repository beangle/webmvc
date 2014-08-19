package org.beangle.webmvc.execution

import org.beangle.webmvc.dispatch.ActionMapping

trait Handler {
  def action: AnyRef
  def handle(mapping: ActionMapping): Any
}

trait Interceptor {

  def intercept(handler: Handler)

  def preHandle(handler: Object): Boolean

  def postHandle(handler: Object, result: Any): Unit
}
