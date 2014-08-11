package org.beangle.webmvc.spring.handler

import org.beangle.commons.lang.Strings
import org.beangle.webmvc.action.ActionSupport
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.route.RouteService
import org.beangle.webmvc.route.impl.{ DefaultViewMapper, MethodHandler }
import org.springframework.web.servlet.{ HandlerAdapter, ModelAndView }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.context.ContextHolder

class ConventionHandlerAdapter(routeService: RouteService) extends HandlerAdapter {

  /**
   * Just support Support subclass
   */
  override def supports(handler: Object): Boolean = {
    handler.isInstanceOf[ActionSupport]
  }

  override def handle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): ModelAndView = {
    val am = ContextHolder.context.mapping
    val result = String.valueOf(am.handler.handle(am.params))
    if (Strings.contains(result, ":")) {
      null
    } else {
      am.handler match {
        case mh: MethodHandler =>
          new ModelAndView(routeService.mapView(mh.action.getClass.getName, DefaultViewMapper.defaultView(mh.method.getName, result)), null)
        case _ => null
      }
    }
  }

  override def getLastModified(request: HttpServletRequest, handler: Object): Long = {
    -1L
  }
}