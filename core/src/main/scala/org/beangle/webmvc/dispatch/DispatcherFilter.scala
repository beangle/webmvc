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

import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.logging.Logging
import org.beangle.commons.i18n.TextResourceProvider
import org.beangle.webmvc.context.{ ActionContextHelper, ContainerHelper, LocaleResolver }
import org.beangle.webmvc.execution.InvocationReactor
import javax.servlet.ServletConfig
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }
import javax.servlet.Filter
import javax.servlet.FilterConfig
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.beangle.commons.io.ClasspathResourceLoader
import org.beangle.commons.web.resource.impl.PathResolverImpl
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.resource.ResourceProcessor
import org.beangle.commons.web.resource.filter.HeaderFilter

class DispatcherFilter extends Filter with Logging {

  var defaultEncoding = "utf-8"
  var mapper: RequestMapper = _
  var reactor: InvocationReactor = _
  var localeResolver: LocaleResolver = _
  var textResourceProvider: TextResourceProvider = _
  var staticPattern: String = "/static/"
  var processor: ResourceProcessor = _

  override def init(config: FilterConfig): Unit = {
    val context = ContainerHelper.get
    mapper = context.getBean(classOf[RequestMapper]).get
    reactor = context.getBean(classOf[InvocationReactor]).get
    localeResolver = context.getBean(classOf[LocaleResolver]).get
    textResourceProvider = context.getBean(classOf[TextResourceProvider]).get
    processor = context.getBean(classOf[ResourceProcessor]) match {
      case Some(p) => p
      case None =>
        val p = new ResourceProcessor(new ClasspathResourceLoader, new PathResolverImpl())
        p.filters = List(new HeaderFilter)
        p
    }
  }

  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    val request = req.asInstanceOf[HttpServletRequest]
    val response = res.asInstanceOf[HttpServletResponse]
    if (request.getServletPath.startsWith(staticPattern)) {
      val contextPath = request.getContextPath
      val uri =
        if (!(contextPath.equals("") || contextPath.equals("/"))) {
          Strings.substringAfter(request.getRequestURI, contextPath)
        } else request.getRequestURI
      processor.process(uri, request, response)
    } else {
      request.setCharacterEncoding(defaultEncoding)
      mapper.resolve(request) match {
        case Some(rm) =>
          ActionContextHelper.build(request, response, rm, localeResolver, textResourceProvider)
          reactor.invoke(rm.handler, rm.action)
        case None => chain.doFilter(req, res)
      }
    }
  }

  override def destroy(): Unit = {}

}