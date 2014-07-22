package org.beangle.webmvc.struts2.config

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.struts2.config.action.IndexAction
import org.beangle.webmvc.struts2.config.action.IndexAction
import org.beangle.webmvc.struts2.config.action.IndexAction

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[IndexAction])
  }
}