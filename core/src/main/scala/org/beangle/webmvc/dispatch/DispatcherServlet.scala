package org.beangle.webmvc.dispatch

import org.beangle.commons.inject.Containers
import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.text.i18n.TextResourceProvider
import org.beangle.webmvc.context.{ ActionContextHelper, LocaleResolver }
import org.beangle.webmvc.execution.InvocationReactor

import javax.servlet.ServletConfig
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }

class DispatcherServlet extends HttpServlet {

  var mapper: RequestMapper = _
  var reactor: InvocationReactor = _
  var localeResolver: LocaleResolver = _
  var textResourceProvider: TextResourceProvider = _

  override def init(config: ServletConfig): Unit = {
    Containers.children.values foreach { child =>
      mapper = child.getBean(classOf[RequestMapper]).get
      reactor = child.getBean(classOf[InvocationReactor]).get
      localeResolver = child.getBean(classOf[LocaleResolver]).get
      textResourceProvider = child.getBean(classOf[TextResourceProvider]).get
    }
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