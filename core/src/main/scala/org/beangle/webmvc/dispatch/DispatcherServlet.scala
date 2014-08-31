package org.beangle.webmvc.dispatch

import org.beangle.commons.inject.Container
import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.text.i18n.TextResourceProvider
import org.beangle.webmvc.context.{ ActionContextHelper, LocaleResolver }
import org.beangle.webmvc.execution.InvocationReactor
import javax.servlet.ServletConfig
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.context.ContainerHelper

class DispatcherServlet extends HttpServlet {

  var mapper: RequestMapper = _
  var reactor: InvocationReactor = _
  var localeResolver: LocaleResolver = _
  var textResourceProvider: TextResourceProvider = _

  override def init(config: ServletConfig): Unit = {
    val context = ContainerHelper.get
    mapper = context.getBean(classOf[RequestMapper]).get
    reactor = context.getBean(classOf[InvocationReactor]).get
    localeResolver = context.getBean(classOf[LocaleResolver]).get
    textResourceProvider = context.getBean(classOf[TextResourceProvider]).get
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    mapper.resolve(request) match {
      case Some(rm) =>
        ActionContextHelper.build(request, response, rm, localeResolver, textResourceProvider)
        reactor.invoke(rm.handler, rm.action)
      case None => response.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }
  }

  override def destroy(): Unit = {}

}