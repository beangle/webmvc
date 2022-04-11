/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.dispatch

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import jakarta.servlet.{GenericServlet, ServletConfig, ServletRequest, ServletResponse}
import org.beangle.cdi.Container
import org.beangle.commons.logging.Logging
import org.beangle.web.action.dispatch.RequestMapper
import org.beangle.web.action.util.Resources
import org.beangle.web.servlet.multipart.StandardMultipartResolver
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.config.{Buildable, Configurer}
import org.beangle.webmvc.context.ActionContextBuilder
import org.beangle.webmvc.execution.ContextAwareHandler

class Dispatcher(configurer: Configurer, mapper: RequestMapper, actionContextBuilder: ActionContextBuilder)
  extends GenericServlet with Logging {

  var defaultEncoding = "utf-8"

  var index: String = _

  def this(container: Container) = {
    this(container.getBean(classOf[Configurer]).get, container.getBean(classOf[RequestMapper]).get, container.getBean(classOf[ActionContextBuilder]).get)
  }

  override def init(config: ServletConfig): Unit = {
    //1. build configuration and mapper
    Buildable.build(configurer,mapper)
    // 2. find index
    val indexFile =
      List("/index.html", "/index.htm", "/index.jsp") find { i =>
        null != config.getServletContext.getResource(i)
      }
    indexFile match {
      case None => if (null != mapper.resolve("/index")) this.index = "/index"
      case Some(i) => this.index = i
    }
  }

  override def service(req: ServletRequest, res: ServletResponse): Unit = {
    val request = req.asInstanceOf[HttpServletRequest]
    val response = res.asInstanceOf[HttpServletResponse]
    request.setCharacterEncoding(defaultEncoding)
    val servletPath = RequestUtils.getServletPath(request)
    mapper.resolve(servletPath, request) match {
      case Some(hh) =>
        val handler = hh.handler
        if (handler.isInstanceOf[ContextAwareHandler]) {
          actionContextBuilder.build(request, response, handler, hh.params)
          try {
            handler.handle(request, response)
          } finally {
            StandardMultipartResolver.cleanup(request)
          }
        } else {
          handler.handle(request, response)
        }
      case None => handleUnknown(servletPath, request, response)
    }
  }

  protected def handleUnknown(servletPath: String, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    if (servletPath.isEmpty || servletPath == "/") {
      if (null != this.index) {
        response.sendRedirect(request.getContextPath + this.index)
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      }
    } else {
      Resources.deliver(response, request.getServletContext, servletPath)
    }
  }

  override def destroy(): Unit = {}
}
