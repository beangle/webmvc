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

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.dispatch.RequestMapper
import org.beangle.webmvc.view.TagLibraryProvider
import org.beangle.webmvc.view.impl.ViewResolverRegistry
import org.beangle.commons.inject.ContainerRefreshedHook
import org.beangle.commons.inject.Container

class ActionLauncher extends ContainerRefreshedHook {

  var serializerManager: SerializerManager = _
  var viewResolverRegistry: ViewResolverRegistry = _
  var requestMapper: RequestMapper = _
  var tagLibraryProvider: TagLibraryProvider = _

  override def notify(container: Container): Unit = {
    if (null != viewResolverRegistry) viewResolverRegistry.start(container)
    if (null != serializerManager) serializerManager.start(container)
    if (null != tagLibraryProvider) tagLibraryProvider.start(container)
    if (null != requestMapper) requestMapper.start(container)
  }
}