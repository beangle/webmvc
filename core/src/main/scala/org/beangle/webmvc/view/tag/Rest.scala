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

package org.beangle.webmvc.view.tag

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.functor.{NotEmpty, NotZero}
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.execution.Handler

class Rest(uriRender: ActionUriRender) {

  def save_url(obj: AnyRef): String = {
    val id: Any = Properties.get(obj, "id")
    if (isValid(id)) {
      uriRender.render(Handler.mapping, "!update?id=" + id)
    } else {
      uriRender.render(Handler.mapping, "!save")
    }
  }

  def info_url(obj: AnyRef): String = {
    val id: Any = Properties.get(obj, "id")
    uriRender.render(Handler.mapping, "!info?id=" + id)
  }

  def save(obj: AnyRef): String = {
    val id: Any = Properties.get(obj, "id")
    if (isValid(id)) {
      "!update?id=" + id
    } else {
      "!save"
    }
  }

  def info(obj: AnyRef): String = {
    val id: Any = Properties.get(obj, "id")
    "!info?id=" + id
  }

  private def isValid(id: Any): Boolean = {
    id match {
      case null => false
      case n: java.lang.Number => NotZero(n)
      case _ => NotEmpty(id.toString)
    }
  }
}
