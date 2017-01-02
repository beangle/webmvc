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
package org.beangle.webmvc.context

import java.{ util => ju }
import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.lang.functor.Predicate
import org.beangle.webmvc.config.Configurer
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.execution.Handler

@spi
trait LocaleResolver {
  def resolve(request: HttpServletRequest): ju.Locale
  def setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: ju.Locale): Unit
}

object ActionFinder {
  /**
   * Test whether the class is a action class.
   * <ul>
   * <li>Ends with suffix</li>
   * <li>In one of given profiles</li>
   * </ul>
   */
  class Test(configurer: Configurer) extends Predicate[Class[_]] {
    def apply(clazz: Class[_]): Boolean = {
      null != configurer.getProfile(clazz.getName)
    }
  }
}
/**
 * Find Action from ObjectFactory
 */
@spi
trait ActionFinder {

  def actions(test: ActionFinder.Test): Seq[Object]

}

@spi
trait Argument {

  def name: String

  def value(context: ActionContext): AnyRef

  def required: Boolean

  def defaultValue: Any
}

@spi
trait ActionContextBuilder {
  def build(request: HttpServletRequest, response: HttpServletResponse, handler: Handler, params: collection.Map[String, Any]): ActionContext
}

@spi
trait ActionContextInitializer {
  def init(context: ActionContext): Unit
}
