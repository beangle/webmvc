/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.webmvc.spring

import org.beangle.commons.inject.{ Container, ContainerRefreshedHook }
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.execution.{ Handler, OncePerRequestInterceptor }
import org.hibernate.SessionFactory
import org.beangle.spring.hibernate.internal.SessionUtils

@description("打开Hibernate Session拦截器")
class OpenSessionInViewInterceptor extends OncePerRequestInterceptor with ContainerRefreshedHook {

  var factories: Iterable[SessionFactory] = Seq.empty

  def notify(context: Container) {
    this.factories = context.parent.getBeans(classOf[SessionFactory]).values
  }

  override def doPreHandle(context: ActionContext, handler: Handler): Boolean = {
    factories.foreach { sf => SessionUtils.enableBinding(sf) }
    true
  }

  override def doPostHandle(context: ActionContext, handler: Handler, result: Any): Unit = {
    factories.foreach { sf =>
      SessionUtils.disableBinding(sf)
      SessionUtils.closeSession(sf)
    }
  }

}
