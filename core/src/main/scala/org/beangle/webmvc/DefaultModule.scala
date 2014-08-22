package org.beangle.webmvc

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.commons.text.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.webmvc.config.impl.DefaultConfigurer
import org.beangle.webmvc.context.impl.{ ActionTextResourceProvider, ParamLocaleResolver }
import org.beangle.webmvc.dispatch.impl.{ DefaultActionMappingBuilder, HierarchicalUrlMapper }
import org.beangle.webmvc.execution.impl.DefaultInvocationReactor
import org.beangle.webmvc.view.freemarker.{ FreemarkerTemplateEngine, HierarchicalTemplateResolver }
import org.beangle.webmvc.view.impl.{ DefaultViewBuilder, DefaultViewPathMapper, ForwardActionViewBuilder, ForwardActionViewRender, FreemarkerConfigurer, FreemarkerViewBuilder, FreemarkerViewResolver, RedirectActionViewBuilder, RedirectActionViewRender }
import org.beangle.webmvc.view.tag.BeangleTagLibrary
import org.beangle.webmvc.context.impl.ContainerActionFinder

class DefaultModule extends AbstractBindModule {

  protected override def binding() {

    bind(classOf[ParamLocaleResolver])
    bind(classOf[DefaultConfigurer], classOf[DefaultViewPathMapper], classOf[DefaultActionMappingBuilder])
    bind(classOf[DefaultInvocationReactor], classOf[HierarchicalUrlMapper])
    bind(classOf[FreemarkerConfigurer], classOf[HierarchicalTemplateResolver],
      classOf[FreemarkerViewResolver], classOf[FreemarkerViewBuilder])

    bind(classOf[ContainerActionFinder])
    bind(classOf[ForwardActionViewRender], classOf[ForwardActionViewBuilder])
    bind(classOf[RedirectActionViewBuilder], classOf[RedirectActionViewRender])
    bind(classOf[DefaultTextFormater], classOf[DefaultTextBundleRegistry])
    bind(classOf[FreemarkerTemplateEngine])
    bind(classOf[DefaultViewBuilder])

    bind(classOf[ActionTextResourceProvider], classOf[DefaultTextFormater], classOf[DefaultTextBundleRegistry])
    bind("b", classOf[BeangleTagLibrary])
    
  }
}