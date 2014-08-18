package org.beangle.webmvc.spring.handler

import org.beangle.webmvc.api.context.ContextHolder
import org.springframework.web.servlet.{ HandlerInterceptor, ModelAndView }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

/**
 * Process Flash in context
 */
object FlashInterceptor extends HandlerInterceptor {

  override def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): Boolean = {
    ContextHolder.context.response = response
    true
  }

  override def postHandle(req: HttpServletRequest, res: HttpServletResponse, handler: Object, mv: ModelAndView): Unit = {
  }

  override def afterCompletion(req: HttpServletRequest, res: HttpServletResponse, handler: Object, ex: Exception): Unit = {
    //FIXME inner forward will invoke twice
    val flash = ContextHolder.context.flash
    if (null != flash) flash.nextToNow()
  }
}