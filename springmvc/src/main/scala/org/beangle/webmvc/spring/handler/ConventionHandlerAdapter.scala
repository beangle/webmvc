package org.beangle.webmvc.spring.handler

import org.beangle.commons.lang.Strings
import org.beangle.webmvc.action.ActionSupport
import org.beangle.webmvc.route.RouteService
import org.beangle.webmvc.route.impl.DefaultURIResolver
import org.springframework.web.servlet.{ HandlerAdapter, ModelAndView }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.route.ActionMapping
import org.beangle.webmvc.spring.handler.Constants._

class ConventionHandlerAdapter(routeService: RouteService) extends HandlerAdapter {
  val resolver = new DefaultURIResolver

  /**
   * Just support Support subclass
   */
  override def supports(handler: Object): Boolean = {
    handler.isInstanceOf[ActionSupport]
  }

  override def handle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): ModelAndView = {
    val am = request.getAttribute(ActionMappingName).asInstanceOf[ActionMapping]
    request.removeAttribute(ActionMappingName)
    val method = handler.getClass().getMethod(am.method)
    val result = method.invoke(handler).toString
    if (Strings.contains(result, ":")) {
      null
    } else {
      import scala.collection.JavaConversions._
      val view = routeService.mapView(handler.getClass.getName, am.method, result.toString())
      new ModelAndView(view, new java.util.HashMap[String, String])
    }
  }

  override def getLastModified(request: HttpServletRequest, handler: Object): Long = {
    -1L
  }
}