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

import org.beangle.commons.io.ClasspathResourceLoader
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.resource.ResourceProcessor
import org.beangle.commons.web.resource.filter.HeaderFilter
import org.beangle.commons.web.resource.impl.PathResolverImpl
import org.beangle.webmvc.context.{ ActionContextBuilder, ContainerHelper }
import javax.servlet.{ Filter, FilterChain, FilterConfig, ServletRequest, ServletResponse }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.config.Configurer

class Dispatcher extends Filter with Logging {

  var defaultEncoding = "utf-8"
  var staticPattern: String = "/static/"

  var mapper: RequestMapper = _
  var actionContextBuilder: ActionContextBuilder = _
  var processor: ResourceProcessor = _

  override def init(config: FilterConfig): Unit = {
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
          actionContextBuilder.build(request, response, rm.handler, rm.params)
          rm.handler.handle(request, response)
        case None => chain.doFilter(req, res)
      }
    }
  }

  override def destroy(): Unit = {}

}