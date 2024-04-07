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

package org.beangle.webmvc.inspect

import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description
import org.beangle.web.action.support.ActionSupport
import org.beangle.web.action.view.View
import org.beangle.cdi.Container
/**
 * @author chaostone
 */
@description("Beange CDI 配置查看器")
class ContainerAction extends ActionSupport {

  def index(): View = {
    var container = Container.get("web")
    val parent = get("parent", "")
    if (Strings.isNotEmpty(parent)) container = container.parent
    put("beanNames", container.keys)
    put("container", container)
    forward()
  }

}
