package org.beangle.webmvc.execution.interceptors

import org.beangle.webmvc.api.context.{ ContextHolder, Flash }
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.execution.{ Handler, Interceptor }

/**
 * Process Flash in context
 */
class FlashInterceptor extends Interceptor {

  override def preHandle(handler: Handler): Boolean = {
    true
  }

  override def postHandle(handler: Handler, mv: Any): Unit = {
    //FIXME inner forward will invoke twice
    val context = ContextHolder.context
    val flash = context.flashMap
    if (null != flash) flash.nextToNow()
  }

}