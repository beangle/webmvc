package org.beangle.webmvc.dispatch.impl

import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.dispatch.{ HandlerHolder, Route, RouteProvider }
import org.beangle.webmvc.execution.{ MappingHandler, InvokerBuilder }
import org.beangle.webmvc.view.impl.ViewManager

/**
 * @author chaostone
 */
@description("缺省的路由提供者")
class DefaultRouteProvider extends RouteProvider with Logging {

  var configurer: Configurer = _

  var invokerBuilder: InvokerBuilder = _

  var viewManager: ViewManager = _

  override def routes: Iterable[Route] = {
    val watch = new Stopwatch(true)
    val results = new collection.mutable.ArrayBuffer[Route]
    configurer.actionMappings foreach {
      case (name, am) =>
        am.mappings foreach {
          case (n, mapping) =>
            val handler = new MappingHandler(mapping, invokerBuilder.build(am.action, mapping), viewManager)
            results += new Route(mapping.httpMethod, mapping.url, handler)
            if (mapping.name == "index") results += new Route(mapping.httpMethod, mapping.action.name, handler)
        }
    }
    results
  }

}