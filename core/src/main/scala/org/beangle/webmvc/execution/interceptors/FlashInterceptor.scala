package org.beangle.webmvc.execution.interceptors

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.execution.{ Handler, OncePerRequestInterceptor }

/**
 * Process Flash in context
 */
@description("请求之间传递参数的flash拦截器")
class FlashInterceptor extends OncePerRequestInterceptor {

  override def doPostHandle(context: ActionContext, handler: Handler, mv: Any): Unit = {
    val flash = context.flashMap
    if (null != flash) flash.nextToNow()
  }

}