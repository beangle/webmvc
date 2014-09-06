package org.beangle.webmvc.execution

import org.beangle.commons.lang.primitive.MutableInt
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.config.ActionMapping

trait Handler {
  def action: AnyRef
  def handle(mapping: ActionMapping): Any
}

trait Interceptor {

  def preHandle(context: ActionContext, handler: Handler): Boolean

  def postHandle(context: ActionContext, handler: Handler, result: Any): Unit
}

trait OncePerRequestInterceptor extends Interceptor {
  final val attributeName = getClass.getName + "_count"

  override final def preHandle(context: ActionContext, handler: Handler): Boolean = {
    var count = context.request.getAttribute(attributeName).asInstanceOf[MutableInt]
    if (null == count) {
      count = new MutableInt
      context.request.setAttribute(attributeName, count)
    }
    if (count.increment() == 1) doPreHandle(context, handler) else true
  }

  def doPreHandle(context: ActionContext, handler: Handler): Boolean = {
    true
  }

  def doPostHandle(context: ActionContext, handler: Handler, result: Any): Unit = {

  }

  override final def postHandle(context: ActionContext, handler: Handler, result: Any): Unit = {
    var count = context.request.getAttribute(attributeName).asInstanceOf[MutableInt]
    if (count.decrement() == 0) doPostHandle(context, handler, result)
  }
}
