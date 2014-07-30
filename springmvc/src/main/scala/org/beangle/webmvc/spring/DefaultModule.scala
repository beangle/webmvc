package org.beangle.webmvc.spring

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.route.impl.RouteServiceImpl
import org.beangle.webmvc.spring.handler.ConventionHandlerMapping
import org.beangle.webmvc.spring.handler.ConventionHandlerAdapter
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver
import org.beangle.webmvc.spring.mvc.BeangleFreeMarkerConfigurer

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[RouteServiceImpl])
    bind("handlerMapping", classOf[ConventionHandlerMapping])
    bind("handlerAdapter", classOf[ConventionHandlerAdapter])
    bind("localeResolver", classOf[ConventionLocaleResolver])
    bind("viewResolver", classOf[FreeMarkerViewResolver]).property("cache", true)
      .property("prefix", "")
      .property("suffix", ".ftl")
      .property("exposeRequestAttributes", true)
      .property("exposeSessionAttributes", true)

    bind("freeMarkerConfig", classOf[BeangleFreeMarkerConfigurer])
  }
}