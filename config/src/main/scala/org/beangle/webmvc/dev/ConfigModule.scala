package org.beangle.webmvc.dev

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.dev.action.ConfigAction

class ConfigModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[ConfigAction])
  }
}