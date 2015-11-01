/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.dispatch

import java.io.{ File, FileInputStream }
import org.beangle.commons.activation.MimeTypeProvider
import org.beangle.commons.io.{ ClasspathResourceLoader, IOs }
import org.beangle.commons.lang.Strings.{ isNotEmpty, substringAfter, substringAfterLast }
import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.resource.ResourceProcessor
import org.beangle.commons.web.resource.filter.HeaderFilter
import org.beangle.commons.web.resource.impl.PathResolverImpl
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.{ ActionContextBuilder, ContainerHelper }
import javax.servlet.{ GenericServlet, ServletConfig, ServletRequest, ServletResponse }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.web.multipart.StandardMultipartResolver

class Dispatcher extends GenericServlet with Logging {

  var defaultEncoding = "utf-8"
  var staticPattern: String = "/static/"

  var mapper: RequestMapper = _
  var actionContextBuilder: ActionContextBuilder = _
  var processor: ResourceProcessor = _

  override def init(config: ServletConfig): Unit = {
    val context = ContainerHelper.get

    //1. build configuration
    context.getBean(classOf[Configurer]).get.build()

    mapper = context.getBean(classOf[RequestMapper]).get
    // 2. build mapper
    mapper.build()

    actionContextBuilder = context.getBean(classOf[ActionContextBuilder]).get
    processor = context.getBean(classOf[ResourceProcessor]) match {
      case Some(p) => p
      case None =>
        val p = new ResourceProcessor(new ClasspathResourceLoader, new PathResolverImpl())
        p.filters = List(new HeaderFilter)
        p
    }
  }

  override def service(req: ServletRequest, res: ServletResponse): Unit = {
    val request = req.asInstanceOf[HttpServletRequest]
    val response = res.asInstanceOf[HttpServletResponse]
    val servletPath = RequestUtils.getServletPath(request)
    if (servletPath.startsWith(staticPattern)) {
      val contextPath = request.getContextPath
      val uri =
        if (!(contextPath.equals("") || contextPath.equals("/"))) substringAfter(request.getRequestURI, contextPath) else request.getRequestURI
      processor.process(uri, request, response)
    } else {
      request.setCharacterEncoding(defaultEncoding)
      mapper.resolve(request) match {
        case Some(rm) =>
          actionContextBuilder.build(request, response, rm.handler, rm.params)
          try {
            rm.handler.handle(request, response)
          } finally {
            StandardMultipartResolver.cleanup(request)
          }
        case None => handleUnknown(servletPath, request, response)
      }
    }
  }

  protected def handleUnknown(servletPath: String, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val filePath = request.getServletContext.getRealPath(servletPath)
    val p = new File(filePath)
    if (p.exists) {
      val ext = substringAfterLast(filePath, ".")
      if (isNotEmpty(ext)) MimeTypeProvider.getMimeType(ext) foreach (m => response.setContentType(m.toString))
      IOs.copy(new FileInputStream(p), response.getOutputStream)
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
    }
  }

  override def destroy(): Unit = {}

}