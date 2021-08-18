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

package org.beangle.webmvc.showcase.action.config.hibernate

import org.beangle.data.hibernate.spring.LocalSessionFactoryBean
import org.hibernate.SessionFactory
import org.beangle.cdi.Container

class SessionFactoryHelper(container: Container) {
  val factories: Map[Any, LocalSessionFactoryBean] =
    container.getBeans(classOf[LocalSessionFactoryBean]).map {
      case (k, v) =>
        var name = k
        name = name.replace(".", "_")
        name = name.replace("#", "_")
        (name, v)
    }

  def getSessionFactory(id: String): SessionFactory = {
    factories(id).result
  }

  def getFactory(id: String): LocalSessionFactoryBean = {
    factories(id)
  }
}
