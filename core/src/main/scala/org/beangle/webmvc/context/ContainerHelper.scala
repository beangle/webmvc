package org.beangle.webmvc.context

import org.beangle.commons.inject.Container

object ContainerHelper {

  def get: Container = {
    Container.containers.find { c => c.parent == Container.ROOT && c.parent != null } match {
      case Some(c) => c
      case None => throw new RuntimeException("Cannot find container from Containers")
    }
  }
}