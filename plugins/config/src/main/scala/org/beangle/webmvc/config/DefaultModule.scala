package org.beangle.webmvc.config

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.config.action.{ ContainerAction, DependencyAction, FreemarkerAction, HibernateAction, IndexAction, MvcAction, WebinitAction }
class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[MvcAction], classOf[ContainerAction], classOf[FreemarkerAction], classOf[HibernateAction],
      classOf[IndexAction], classOf[DependencyAction], classOf[WebinitAction])
  }
}