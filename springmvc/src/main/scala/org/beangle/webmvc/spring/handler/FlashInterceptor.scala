package org.beangle.webmvc.spring.handler

import org.springframework.web.servlet.HandlerInterceptor
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.context.ActionContext
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.springframework.web.servlet.ModelAndView

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