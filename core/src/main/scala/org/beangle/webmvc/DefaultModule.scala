package org.beangle.webmvc

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.commons.text.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.webmvc.config.impl.{ DefaultActionMappingBuilder, DefaultConfigurer, XmlProfileProvider }
import org.beangle.webmvc.context.impl.{ ActionTextResourceProvider, ContainerActionFinder, ParamLocaleResolver }
import org.beangle.webmvc.dispatch.impl.{ DefaultActionUriRender, HierarchicalUrlMapper }
import org.beangle.webmvc.execution.impl.DefaultInvocationReactor
import org.beangle.webmvc.view.freemarker.{ FreemarkerTemplateEngine, HierarchicalTemplateResolver }
import org.beangle.webmvc.view.impl.{ ContainerTaglibraryProvider, DefaultViewBuilder, ForwardActionViewBuilder, ForwardActionViewRender, FreemarkerConfigurer, FreemarkerViewBuilder, FreemarkerViewResolver, RedirectActionViewBuilder, RedirectActionViewRender }
import org.beangle.webmvc.view.tag.BeangleTagLibrary
import org.beangle.webmvc.view.template.DefaultTemplatePathMapper

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    //config
    bind("mvc.ProfileProvider.default", classOf[XmlProfileProvider])
    bind("mvc.Configurer.default", classOf[DefaultConfigurer])
    bind("mvc.ActionMappingBuilder.default", classOf[DefaultActionMappingBuilder])
    bind("mvc.ActionFinder.default", classOf[ContainerActionFinder])

    //template
    bind("mvc.FreemarkerConfigurer.default", classOf[FreemarkerConfigurer])
    bind("mvc.TemplatePathMapper.default", classOf[DefaultTemplatePathMapper])
    bind("mvc.TemplateResolver.freemarker", classOf[HierarchicalTemplateResolver])
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine])

    //view
    bind("mvc.ViewResolver.freemarker", classOf[FreemarkerViewResolver])
    bind("mvc.ViewBuilder.default", classOf[DefaultViewBuilder])
    bind("mvc.TypeViewBuilder.freemarker", classOf[FreemarkerViewBuilder])
    bind("mvc.TypeViewBuilder.chain", classOf[ForwardActionViewBuilder])
    bind("mvc.TypeViewBuilder.redirect", classOf[RedirectActionViewBuilder])
    bind("mvc.ViewRender.chain", classOf[ForwardActionViewRender])
    bind("mvc.ViewRender.redirect", classOf[RedirectActionViewRender])
    bind("mvc.TaglibraryProvider.default", classOf[ContainerTaglibraryProvider])
    bind("mvc.Taglibrary.b", classOf[BeangleTagLibrary])

    //dispatch
    bind("mvc.ActionUriRender.default", classOf[DefaultActionUriRender])
    bind("mvc.RequestMapper.default", classOf[HierarchicalUrlMapper])

    //execution
    bind("mvc.InvocationReactor.default", classOf[DefaultInvocationReactor])

    //context
    bind("mvc.TextResourceProvider.default", classOf[ActionTextResourceProvider])
    bind("mvc.TextFormater.default", classOf[DefaultTextFormater])
    bind("mvc.TextBundleRegistry.default", classOf[DefaultTextBundleRegistry])
    bind("mvc.LocaleResolver.default", classOf[ParamLocaleResolver])
  }
}