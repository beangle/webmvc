package org.beangle.webmvc.spring

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.dispatch.impl.StaticResourceRouteProvider

/**
 * @author chaostone
 */
class DefaultModule extends AbstractBindModule {
  protected override def binding(): Unit = {
    bind(classOf[StaticResourceRouteProvider])
  }
}