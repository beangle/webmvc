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

package org.beangle.webmvc.config

import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.lang.functor.Predicate

object ActionFinder {
  /**
   * Test whether the class is a action class.
   * <ul>
   * <li>Ends with suffix</li>
   * <li>In one of given profiles</li>
   * </ul>
   */
  class Test(configurer: Configurator) extends Predicate[Class[_]] {
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
