package org.beangle.webmvc.spring

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.route.impl.RouteServiceImpl
import org.beangle.webmvc.spring.handler.ConventionHandlerMapping
import org.beangle.webmvc.spring.handler.ConventionHandlerAdapter
import org.beangle.webmvc.spring.mvc.view.FreeMarkerConfigurer
import org.beangle.webmvc.spring.mvc.view.FreeMarkerViewResolver
import org.beangle.webmvc.view.freemarker.FreemarkerTemplateEngine
import org.beangle.webmvc.view.tag.BeangleTagLibrary
import org.beangle.webmvc.route.impl.HierarchicalUrlMapper

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[RouteServiceImpl])
    bind("handlerMapping", classOf[ConventionHandlerMapping])
    bind("handlerAdapter", classOf[ConventionHandlerAdapter])
    bind("localeResolver", classOf[ParamLocaleResolver])
    bind("requestMapper", classOf[HierarchicalUrlMapper])
    bind("viewResolver", classOf[FreeMarkerViewResolver]).property("cache", true)
      .property("prefix", "")
      .property("suffix", ".ftl")
      .property("exposeRequestAttributes", true)
      .property("exposeSessionAttributes", true)

    bind("freeMarkerConfig", classOf[FreeMarkerConfigurer])

    bind(classOf[FreemarkerTemplateEngine])
    bind("b", classOf[BeangleTagLibrary])
  }
}