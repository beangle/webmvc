package org.beangle.webmvc.spring.handler

import org.beangle.commons.lang.Strings.{ contains, substringBefore }
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.route.{ Action, ClassAction, Handler, RequestMapper, RouteService }
import org.beangle.webmvc.route.impl.DefaultViewMapper
import org.springframework.web.servlet.{ HandlerAdapter, ModelAndView }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class ConventionHandlerAdapter(routeService: RouteService) extends HandlerAdapter {

  var mapper: RequestMapper = _
  /**
   * Just support Support subclass
   */
  override def supports(handler: Object): Boolean = {
    handler.isInstanceOf[Handler]
  }

  override def handle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): ModelAndView = {
    val am = ContextHolder.context.mapping
    val result = String.valueOf(am.handler.handle(am.action))
    if (contains(result, ":")) {
      val prefix = substringBefore(result, ":")
      val ca = ContextHolder.context.temp[Object]("dispatch_action").asInstanceOf[ClassAction]
      val url = mapper.antiResolve(ca.clazz, ca.method) match {
        case Some(rm) => Action.toURIAction(ca, rm.action, ContextHolder.context.params).url
        case None => throw new RuntimeException(s"Cannot find action mapping for ${ca.clazz.getName} ${ca.method}")
      }
      if (prefix == "chain") {
        request.getRequestDispatcher(url).forward(request, response)
      } else if (prefix == "redirectAction") {
        val finalLocation = if (request.getContextPath.length > 1) request.getContextPath + url else url
        val encodedLocation = response.encodeRedirectURL(finalLocation)
        response.sendRedirect(encodedLocation)
      }
      null
    } else {
      new ModelAndView(routeService.mapView(am.action.clazz.getName, DefaultViewMapper.defaultView(am.action.method, result)), null)
    }
  }

  override def getLastModified(request: HttpServletRequest, handler: Object): Long = {
    -1L
  }
}