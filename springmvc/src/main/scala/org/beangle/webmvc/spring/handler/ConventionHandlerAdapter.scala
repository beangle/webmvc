package org.beangle.webmvc.spring.handler

import org.beangle.commons.lang.time.Stopwatch
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.execution.{ Handler, InvocationReactor }
import org.springframework.web.servlet.{ HandlerAdapter, ModelAndView }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class ConventionHandlerAdapter(configurer: Configurer) extends HandlerAdapter {

  var invocationReactor: InvocationReactor = _
  /**
   * Just support Support subclass
   */
  override def supports(handler: Object): Boolean = {
    handler.isInstanceOf[Handler]
  }

  override def handle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): ModelAndView = {
    ContextHolder.context.response = response
    val am = ActionContextHelper.getMapping(ContextHolder.context)
    invocationReactor.invoke(am.handler, am.action)
    null
  }

  override def getLastModified(request: HttpServletRequest, handler: Object): Long = {
    -1L
  }
}