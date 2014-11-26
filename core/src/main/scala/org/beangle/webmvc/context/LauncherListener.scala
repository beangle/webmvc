package org.beangle.webmvc.context

import org.beangle.commons.inject.Container

trait LauncherListener {
  def start(container: Container): Unit
}