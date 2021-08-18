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

import java.io.{ File, FileInputStream }
import java.net.URL

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.description
import org.beangle.web.action.annotation.action
import org.beangle.web.action.view.View

@description("Hibernate配置查看器")
@action("config/{session_factory_id}")
class ConfigAction extends AbstractAction {

  def index(): View = {
    put("factory", getFactory())
    put("action", this)
    forward()
  }

  def getURLString(url: URL): String = {
    IOs.readString(url.openStream())
  }

}
