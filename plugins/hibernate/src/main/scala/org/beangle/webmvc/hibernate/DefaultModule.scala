package org.beangle.webmvc.hibernate

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.hibernate.action.{ ConfigAction, IndexAction, StatAction }
import org.beangle.webmvc.hibernate.helper.SessionFactoryHelper

object DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[IndexAction], classOf[StatAction], classOf[ConfigAction])
    bind(classOf[SessionFactoryHelper])
  }
}