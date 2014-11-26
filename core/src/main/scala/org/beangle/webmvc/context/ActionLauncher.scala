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