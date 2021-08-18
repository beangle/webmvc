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

package org.beangle.webmvc.support.hibernate

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.lang.annotation.description
import org.beangle.web.servlet.intercept.OncePerRequestInterceptor
import org.beangle.data.hibernate.spring.SessionUtils
import org.hibernate.SessionFactory

@description("打开Hibernate Session拦截器")
class OpenSessionInViewInterceptor(factories: List[SessionFactory]) extends OncePerRequestInterceptor {

  override def doPreInvoke(request: HttpServletRequest, response: HttpServletResponse): Boolean = {
    factories.foreach { sf => SessionUtils.enableBinding(sf) }
    true
  }

  override def doPostInvoke(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    factories.foreach { sf =>
      SessionUtils.disableBinding(sf)
      SessionUtils.closeSession(sf)
    }
  }

}
