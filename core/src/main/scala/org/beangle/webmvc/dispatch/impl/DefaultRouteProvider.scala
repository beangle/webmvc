/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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