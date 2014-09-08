package org.beangle.webmvc

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.commons.text.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.webmvc.config.impl.{ DefaultActionMappingBuilder, DefaultConfigurer, XmlProfileProvider }
import org.beangle.webmvc.context.impl.{ ActionTextResourceProvider, ContainerActionFinder, ParamLocaleResolver }
import org.beangle.webmvc.dispatch.impl.{ DefaultActionUriRender, HierarchicalUrlMapper }
import org.beangle.webmvc.execution.impl.{ DefaultInvocationReactor, MethodHandlerBuilder, StaticHandlerBuilder }
import org.beangle.webmvc.execution.interceptors.FlashInterceptor
import org.beangle.webmvc.view.freemarker.{ FreemarkerConfigurer, HierarchicalTemplateResolver }
import org.beangle.webmvc.view.impl.{ ContainerTaglibraryProvider, DefaultTemplatePathMapper, DefaultViewBuilder, ForwardActionViewBuilder, ForwardActionViewRender, FreemarkerViewBuilder, FreemarkerViewResolver, RedirectActionViewBuilder, RedirectActionViewRender }

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

    //view
    bind("mvc.ViewResolver.freemarker", classOf[FreemarkerViewResolver])
    bind("mvc.ViewBuilder.default", classOf[DefaultViewBuilder])
    bind("mvc.TypeViewBuilder.freemarker", classOf[FreemarkerViewBuilder])
    bind("mvc.TypeViewBuilder.chain", classOf[ForwardActionViewBuilder])
    bind("mvc.TypeViewBuilder.redirect", classOf[RedirectActionViewBuilder])
    bind("mvc.ViewRender.chain", classOf[ForwardActionViewRender])
    bind("mvc.ViewRender.redirect", classOf[RedirectActionViewRender])
    bind("mvc.TaglibraryProvider.default", classOf[ContainerTaglibraryProvider])

    //dispatch
    bind("mvc.ActionUriRender.default", classOf[DefaultActionUriRender])
    bind("mvc.RequestMapper.default", classOf[HierarchicalUrlMapper])

    //execution
    bind("mvc.InvocationReactor.default", classOf[DefaultInvocationReactor])
    bind("mvc.Interceptor.flash", classOf[FlashInterceptor])
    bind("mvc.HandlerBuilder.method", classOf[MethodHandlerBuilder])
    bind("mvc.HandlerBuilder.static", classOf[StaticHandlerBuilder]).primary

    //context
    bind("mvc.TextResourceProvider.default", classOf[ActionTextResourceProvider])
    bind("mvc.TextFormater.default", classOf[DefaultTextFormater])
    bind("mvc.TextBundleRegistry.default", classOf[DefaultTextBundleRegistry])
    bind("mvc.LocaleResolver.default", classOf[ParamLocaleResolver])
  }
}