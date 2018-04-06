/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.dispatch

import java.io.{ File, FileInputStream }
import org.beangle.commons.activation.MimeTypes
import org.beangle.commons.io.{ ClasspathResourceLoader, IOs }
import org.beangle.commons.lang.Strings.{ isNotEmpty, substringAfter, substringAfterLast }
import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.resource.ResourceProcessor
import org.beangle.commons.web.resource.filter.HeaderFilter
import org.beangle.commons.web.resource.impl.PathResolverImpl
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.{ ActionContextBuilder }
import javax.servlet.{ GenericServlet, ServletConfig, ServletRequest, ServletResponse }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.web.multipart.StandardMultipartResolver
import org.beangle.webmvc.execution.ContextAwareHandler
import org.beangle.cdi.Container

class Dispatcher(configurer: Configurer, mapper: RequestMapper, actionContextBuilder: ActionContextBuilder)
  extends GenericServlet with Logging {

  var defaultEncoding = "utf-8"

  var index: String = _

  def this(container: Container) {
    this(container.getBean(classOf[Configurer]).get, container.getBean(classOf[RequestMapper]).get, container.getBean(classOf[ActionContextBuilder]).get)
  }

  override def init(config: ServletConfig): Unit = {
    //1. build configuration
    configurer.build()
    // 2. build mapper
    mapper.build()
    // 3. find index
    val indexFile =
      List("/index.html", "/index.htm", "/index.jsp") find { i =>
        new File(config.getServletContext().getRealPath(i)).exists()
      }
    indexFile match {
      case None => if (null != mapper.resolve("/index")) this.index = "/index"
      case Some(i) => this.index = i
    }
  }

  override def service(req: ServletRequest, res: ServletResponse): Unit = {
    val request = req.asInstanceOf[HttpServletRequest]
    val response = res.asInstanceOf[HttpServletResponse]
    val servletPath = RequestUtils.getServletPath(request)
    request.setCharacterEncoding(defaultEncoding)
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
    if (servletPath.isEmpty) {
      if (null != this.index) {
        response.sendRedirect(request.getContextPath + this.index)
      } else {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      }
    } else {
      findFile(request, servletPath) match {
        case Some(f) =>
          val ext = substringAfterLast(f.getName, ".")
          if (isNotEmpty(ext)) MimeTypes.getMimeType(ext) foreach (m => response.setContentType(m.toString))
          IOs.copy(new FileInputStream(f), response.getOutputStream)
        case None =>
          response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      }
    }
  }

  private def findFile(request: HttpServletRequest, servletPath: String): Option[File] = {
    val filePath = request.getServletContext.getRealPath(servletPath)
    var p = new File(filePath)
    if (p.exists) {
      if (p.isDirectory) {
        val index = new File(p.getAbsolutePath + File.separator + "index.html")
        if (index.exists) Some(index) else None
      } else {
        Some(p)
      }
    } else {
      None
    }
  }

  override def destroy(): Unit = {}

}
