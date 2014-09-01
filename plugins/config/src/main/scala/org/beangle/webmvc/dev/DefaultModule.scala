package org.beangle.webmvc.dev.mvc

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.dev.action.mvc.ConfigAction

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[ConfigAction])
  }
}