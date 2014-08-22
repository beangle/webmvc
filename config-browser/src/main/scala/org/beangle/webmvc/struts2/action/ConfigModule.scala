package org.beangle.webmvc.struts2.action

import org.beangle.commons.inject.bind.AbstractBindModule

class ConfigModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[ConfigAction])
  }
}