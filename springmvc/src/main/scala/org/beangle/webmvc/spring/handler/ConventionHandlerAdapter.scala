package org.beangle.webmvc.spring.handler

import org.beangle.commons.lang.Strings.{ contains, substringBefore }
import org.beangle.webmvc.api.action.ToClass
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.spi.dispatch.{ Handler, RequestMapper }
import org.beangle.webmvc.spi.view.ViewMapper
import org.beangle.webmvc.view.DefaultViewMapper
import org.springframework.web.servlet.{ HandlerAdapter, ModelAndView }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class ConventionHandlerAdapter(configurer: Configurer) extends HandlerAdapter {

  var mapper: RequestMapper = _
  var viewMapper: ViewMapper = _
  /**
   * Just support Support subclass
   */
  override def supports(handler: Object): Boolean = {
    handler.isInstanceOf[Handler]
  }

  override def handle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): ModelAndView = {
    val am = ActionContextHelper.getMapping(ContextHolder.context)
    val result = String.valueOf(am.handler.handle(am.action))
    if (contains(result, ":")) {
      val prefix = substringBefore(result, ":")
      val ca = ContextHolder.context.temp[Object]("dispatch_action").asInstanceOf[ToClass]
      val url = mapper.antiResolve(ca.clazz, ca.method) match {
        case Some(rm) => rm.action.toURI(ca, ContextHolder.context.params).url
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
      val className = am.action.clazz.getName
      new ModelAndView(viewMapper.map(className, DefaultViewMapper.defaultView(am.action.method, result), configurer.getProfile(className)), null)
    }
  }

  override def getLastModified(request: HttpServletRequest, handler: Object): Long = {
    -1L
  }
}