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

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.RouteMapping
import jakarta.servlet.http.HttpServletRequest

@spi
trait RequestMapper {

  def resolve(uri: String, request: HttpServletRequest): Option[HandlerHolder]

  def resolve(uri: String): Option[HandlerHolder]

  def build(): Unit
}

/**
 * Url render
 */
trait ActionUriRender {
  def render(action: RouteMapping, uri: String): String
}

@spi
trait RouteProvider {
  def routes: Iterable[Route]
}
