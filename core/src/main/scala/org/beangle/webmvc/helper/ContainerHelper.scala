package org.beangle.webmvc.helper

import org.beangle.commons.inject.Containers
import org.beangle.commons.inject.Container

object ContainerHelper {

  def get(): Container = {
    val container = if (Containers.children.isEmpty) Containers.root else Containers.children.values.head
    if (null == container) throw new RuntimeException("Cannot find container from Containers")
    container
  }
}