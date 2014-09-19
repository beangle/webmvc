package org.beangle.webmvc.showcase

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.showcase.action.StreamAction

class DefaultModule extends AbstractBindModule {

  protected def binding(): Unit = {
    bind(classOf[StreamAction])
  }
}