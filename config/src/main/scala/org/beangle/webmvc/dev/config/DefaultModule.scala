package org.beangle.webmvc.dev.config

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.dev.config.action.BrowserAction

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[BrowserAction])
  }
}