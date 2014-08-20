package org.beangle.webmvc.spring

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.commons.text.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.webmvc.config.impl.DefaultConfigurer
import org.beangle.webmvc.context.impl.{ ActionTextResourceProvider, ParamLocaleResolver }
import org.beangle.webmvc.dispatch.impl.{ DefaultActionMappingBuilder, HierarchicalUrlMapper }
import org.beangle.webmvc.execution.impl.DefaultInvocationReactor
import org.beangle.webmvc.spring.handler.{ ConventionHandlerAdapter, ConventionHandlerMapping }
import org.beangle.webmvc.view.freemarker.FreemarkerTemplateEngine
import org.beangle.webmvc.view.impl.{ DefaultViewPathMapper, FreemarkerConfigurer, FreemarkerViewResolver }
import org.beangle.webmvc.view.tag.BeangleTagLibrary
import org.beangle.webmvc.view.impl.ForwardActionViewRender
import org.beangle.webmvc.view.impl.RedirectActionViewRender

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind("handlerMapping", classOf[ConventionHandlerMapping])
    bind("handlerAdapter", classOf[ConventionHandlerAdapter])
    bind("localeResolver", classOf[BeangleLocaleResolver])
    bind("paramLocaleResolver", classOf[ParamLocaleResolver])

    bind(classOf[DefaultConfigurer], classOf[DefaultViewPathMapper], classOf[DefaultActionMappingBuilder])
    bind(classOf[DefaultInvocationReactor], classOf[HierarchicalUrlMapper], classOf[FreemarkerConfigurer], classOf[FreemarkerViewResolver])

    bind(classOf[ForwardActionViewRender], classOf[RedirectActionViewRender])
    bind(classOf[DefaultTextFormater], classOf[DefaultTextBundleRegistry])
    bind(classOf[FreemarkerTemplateEngine])
    bind(classOf[ActionTextResourceProvider], classOf[DefaultTextFormater], classOf[DefaultTextBundleRegistry])
    bind("b", classOf[BeangleTagLibrary])
  }
}