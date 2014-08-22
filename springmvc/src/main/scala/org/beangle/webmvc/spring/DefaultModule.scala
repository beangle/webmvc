package org.beangle.webmvc.spring

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.spring.handler.{ ConventionHandlerAdapter, ConventionHandlerMapping }

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind("handlerMapping", classOf[ConventionHandlerMapping])
    bind("handlerAdapter", classOf[ConventionHandlerAdapter])
    bind("localeResolver", classOf[BeangleLocaleResolver])
  }
}