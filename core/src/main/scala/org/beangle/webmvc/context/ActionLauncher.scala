/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import org.beangle.commons.inject.{ Container, ContainerRefreshedHook }
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.dispatch.RequestMapper
import org.beangle.webmvc.view.TagLibraryProvider
import org.beangle.webmvc.view.impl.ViewManager

class ActionLauncher extends ContainerRefreshedHook {

  var viewManager: ViewManager = _
  var requestMapper: RequestMapper = _

  override def notify(container: Container): Unit = {
    if (null != viewManager) viewManager.start(container)
    if (null != requestMapper) requestMapper.start(container)
  }
}