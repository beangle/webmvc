package org.beangle.webmvc.support.hibernate

import org.beangle.cdi.bind.BindModule

class DefaultModule extends BindModule {

  override def binding(): Unit = {
    bind("web.Interceptor.hibernate", classOf[OpenSessionInViewInterceptor])
  }
}
