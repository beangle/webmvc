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
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.config.action

import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.context.ContainerHelper

/**
 * @author chaostone
 */
@description("Beange CDI 配置查看器")
class ContainerAction extends ActionSupport {

  def index(): String = {
    var container = ContainerHelper.get
    val parent = get("parent", "")
    if (Strings.isNotEmpty(parent)) container = container.parent
    put("beanNames", container.keys)
    put("container", container)
    forward()
  }

}