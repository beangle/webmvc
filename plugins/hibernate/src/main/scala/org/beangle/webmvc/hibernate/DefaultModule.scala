package org.beangle.webmvc.hibernate

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.hibernate.action.{ ConfigAction, StatAction }

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[StatAction], classOf[ConfigAction])
  }
}