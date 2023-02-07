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

import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.data.orm.hibernate.LocalSessionFactoryBean
import org.beangle.web.action.support.{ ParamSupport, RouteSupport }
import org.hibernate.SessionFactory

abstract class AbstractAction extends RouteSupport with ParamSupport {

  var helper: SessionFactoryHelper = _

  def getSessionFactory(): SessionFactory = {
    val sfid = get("session_factory_id", "")
    if (isEmpty(sfid)) return null
    else helper.getSessionFactory(sfid)
  }

  def getFactory(): LocalSessionFactoryBean = {
    val sfid = get("session_factory_id", "")
    if (isEmpty(sfid)) return null
    else helper.getFactory(sfid)
  }

}
