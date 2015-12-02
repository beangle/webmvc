package org.beangle.webmvc.dispatch.impl

import org.beangle.commons.bean.Initializing
import org.beangle.commons.http.HttpMethods.GET
import org.beangle.commons.io.ClasspathResourceLoader
import org.beangle.commons.lang.Strings.substringAfter
import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.web.resource.ResourceProcessor
import org.beangle.commons.web.resource.filter.HeaderFilter
import org.beangle.commons.web.resource.impl.PathResolverImpl
import org.beangle.webmvc.dispatch.{ Route, RouteProvider }
import org.beangle.webmvc.execution.Handler

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

/**
 * @author chaostone
 */
class StaticResourceRouteProvider extends RouteProvider with Initializing {
  var patterns: Array[String] = Array("/static/{path*}")
  var processor: ResourceProcessor = _
  private var handler: StaticResourceHandler = _

  def routes: Iterable[Route] = {
    patterns.map(pattern => new Route(GET, pattern, handler)).toList
  }

  def init(): Unit = {
    if (null == processor) {
      processor = new ResourceProcessor(new ClasspathResourceLoader, new PathResolverImpl())
      processor.filters = List(new HeaderFilter)
    }
    handler = new StaticResourceHandler(processor)
  }
}

class StaticResourceHandler(processor: ResourceProcessor) extends Handler {

  def handle(request: HttpServletRequest, response: HttpServletResponse): Any = {
    val ctxPath = request.getContextPath
    val uri =
      if (!(ctxPath.equals("") || ctxPath.equals("/"))) substringAfter(request.getRequestURI, ctxPath) else request.getRequestURI
    processor.process(uri, request, response)
  }
}

