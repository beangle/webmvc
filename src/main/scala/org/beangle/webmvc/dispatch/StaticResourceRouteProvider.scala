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
import org.beangle.commons.bean.Initializing
import org.beangle.commons.io.ClasspathResourceLoader
import org.beangle.commons.lang.Strings.substringAfter
import org.beangle.commons.net.http.HttpMethods.GET
import org.beangle.web.servlet.resource.ResourceProcessor
import org.beangle.web.servlet.resource.filter.HeaderFilter
import org.beangle.webmvc.dispatch.{Route, RouteProvider}
import org.beangle.webmvc.execution.Handler

/**
 * @author chaostone
 */
class StaticResourceRouteProvider extends RouteProvider, Initializing {
  var patterns: Array[String] = Array("/static/{path*}")
  private var processor: ResourceProcessor = _
  private var handler: StaticResourceHandler = _

  def routes: Iterable[Route] = {
    patterns.map(pattern => Route(GET, pattern, handler)).toList
  }

  def init(): Unit = {
    if (null == processor) {
      processor = new ResourceProcessor(new ClasspathResourceLoader)
      processor.filters = List(new HeaderFilter)
    }
    handler = new StaticResourceHandler(processor)
  }
}

class StaticResourceHandler(processor: ResourceProcessor) extends Handler {

  override def handle(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val ctxPath = request.getContextPath
    val uri =
      if (!(ctxPath.equals("") || ctxPath.equals("/"))) substringAfter(request.getRequestURI, ctxPath) else request.getRequestURI
    processor.process(uri, request, response)
  }
}
