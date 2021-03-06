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
package org.beangle.webmvc.execution

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.RouteMapping
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.beangle.webmvc.api.context.ActionContext

@spi
trait InvokerBuilder {
  def build(action: AnyRef, mapping: RouteMapping): Invoker
}

@spi
trait Invoker {
  def invoke(): Any
}

@spi
trait Handler {
  def handle(request: HttpServletRequest, response: HttpServletResponse): Unit
}

trait ContextAwareHandler extends Handler

object Handler {

  val HandlerAttribute = "_handler_"

  def current: Handler = {
    ActionContext.current.stash[Handler](HandlerAttribute)
  }

  def mapping: RouteMapping = {
    current.asInstanceOf[MappingHandler].mapping
  }
}

case class CacheResult(contentType:String, data:Array[Byte])
