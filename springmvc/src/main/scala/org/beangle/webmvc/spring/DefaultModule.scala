package org.beangle.webmvc.spring

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.commons.text.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.webmvc.config.impl.DefaultConfigurer
import org.beangle.webmvc.spring.handler.{ ConventionHandlerAdapter, ConventionHandlerMapping }
import org.beangle.webmvc.spring.mvc.view.{ FreeMarkerConfigurer, FreeMarkerViewResolver }
import org.beangle.webmvc.view.freemarker.FreemarkerTemplateEngine
import org.beangle.webmvc.view.tag.BeangleTagLibrary
import org.beangle.webmvc.dispatch.impl.HierarchicalUrlMapper
import org.beangle.webmvc.dispatch.impl.DefaultActionMappingBuilder
import org.beangle.webmvc.context.impl.ActionTextResourceProvider
import org.beangle.webmvc.view.impl.DefaultViewMapper
import org.beangle.webmvc.context.impl.ParamLocaleResolver

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[DefaultConfigurer], classOf[DefaultViewMapper], classOf[DefaultActionMappingBuilder])
    bind("handlerMapping", classOf[ConventionHandlerMapping])
    bind("handlerAdapter", classOf[ConventionHandlerAdapter])
    bind("localeResolver", classOf[BeangleLocaleResolver])
    bind("paramLocaleResolver", classOf[ParamLocaleResolver])
    bind("requestMapper", classOf[HierarchicalUrlMapper])
    bind("viewResolver", classOf[FreeMarkerViewResolver]).property("cache", true)
      .property("prefix", "")
      .property("suffix", ".ftl")
      .property("exposeRequestAttributes", true)
      .property("exposeSessionAttributes", true)

    bind("freeMarkerConfig", classOf[FreeMarkerConfigurer])

    bind(classOf[DefaultTextFormater], classOf[DefaultTextBundleRegistry])
    bind(classOf[FreemarkerTemplateEngine])
    bind(classOf[ActionTextResourceProvider], classOf[DefaultTextFormater], classOf[DefaultTextBundleRegistry])
    bind("b", classOf[BeangleTagLibrary])
  }
}