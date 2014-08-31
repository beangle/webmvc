package org.beangle.webmvc.context

import org.beangle.commons.inject.Container

object ContainerHelper {

  def get: Container = {
    val container = if (Container.ROOT.children.isEmpty) Container.ROOT else Container.ROOT.children.values.head
    if (null == container) throw new RuntimeException("Cannot find container from Containers")
    container
  }
}