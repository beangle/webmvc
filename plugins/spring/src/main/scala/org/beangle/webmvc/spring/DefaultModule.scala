package org.beangle.webmvc.spring

import org.beangle.commons.inject.bind.AbstractBindModule

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind("mvc.Interceptor.hibernate", classOf[OpenSessionInViewInterceptor])
  }
}