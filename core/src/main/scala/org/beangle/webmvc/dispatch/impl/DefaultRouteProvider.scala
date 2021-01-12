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
package org.beangle.webmvc.dispatch.impl

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.dispatch.{Route, RouteProvider}
import org.beangle.webmvc.execution.{EmptyResponseCache, InvokerBuilder, MappingHandler, ResponseCache}
import org.beangle.webmvc.view.impl.ViewManager

import scala.collection.mutable

/**
 * @author chaostone
 */
@description("缺省的路由提供者")
class DefaultRouteProvider extends RouteProvider with Logging {

  var configurer: Configurer = _

  var invokerBuilder: InvokerBuilder = _

  var viewManager: ViewManager = _

  var responseCache: ResponseCache = EmptyResponseCache

  override def routes: Iterable[Route] = {
    val results = new collection.mutable.ArrayBuffer[Route]
    configurer.actionMappings foreach {
      case (_, am) =>
        am.mappings foreach {
          case (_, mapping) =>
            val handler = new MappingHandler(mapping, invokerBuilder.build(am.action, mapping), viewManager, responseCache)
            results += new Route(mapping.httpMethod, mapping.url, handler)
            stripTailIndex(mapping.url) foreach { short =>
              results += new Route(mapping.httpMethod, short, handler)
            }
        }
    }
    results
  }

  private def stripTailIndex(url: String): collection.Seq[String] = {
    val seqs = Collections.newBuffer[String]
    collectShorts(url, seqs)
    seqs
  }

  @scala.annotation.tailrec
  private def collectShorts(url: String, shorts: mutable.Buffer[String]): Unit = {
    if (url.endsWith("/index")) {
      val short = Strings.substringBeforeLast(url, "/index")
      shorts += short
      collectShorts(short, shorts)
    }
  }
}
