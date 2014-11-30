package org.beangle.webmvc

import org.beangle.commons.inject.bind.{ AbstractBindModule, profile }
import org.beangle.commons.text.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.webmvc.config.impl.{ DefaultActionMappingBuilder, DefaultConfigurer, XmlProfileProvider }
import org.beangle.webmvc.context.impl.{ ActionTextResourceProvider, ContainerActionFinder, DefaultSerializerManager, ParamLocaleResolver }
import org.beangle.webmvc.dispatch.impl.{ DefaultActionUriRender, HierarchicalUrlMapper }
import org.beangle.webmvc.execution.impl.{ DefaultInvocationReactor, DynaMethodHandlerBuilder, MvcRequestConvertor, StaticMethodHandlerBuilder }
import org.beangle.webmvc.execution.interceptors.{ CorsInterceptor, FlashInterceptor }
import org.beangle.webmvc.view.impl.{ ContainerTaglibraryProvider, DefaultTemplatePathMapper, DefaultViewBuilder, ForwardActionViewBuilder, ForwardActionViewRender, RedirectActionViewBuilder, RedirectActionViewRender, StatusViewRender, StreamViewRender }
import org.beangle.webmvc.view.impl.ViewResolverRegistry
import org.beangle.webmvc.context.ActionLauncher

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    //config
    bind("mvc.ProfileProvider.default", classOf[XmlProfileProvider])
    bind("mvc.Configurer.default", classOf[DefaultConfigurer])
    bind("mvc.ActionMappingBuilder.default", classOf[DefaultActionMappingBuilder])
    bind("mvc.ActionFinder.default", classOf[ContainerActionFinder])

    //template
    bind("mvc.TemplatePathMapper.default", classOf[DefaultTemplatePathMapper])

    //view
    bind("mvc.ViewBuilder.default", classOf[DefaultViewBuilder])
    bind("mvc.ViewResolverRegistry", classOf[ViewResolverRegistry])
    bind("mvc.TypeViewBuilder.chain", classOf[ForwardActionViewBuilder])
    bind("mvc.TypeViewBuilder.redirect", classOf[RedirectActionViewBuilder])
    bind("mvc.ViewRender.chain", classOf[ForwardActionViewRender])
    bind("mvc.ViewRender.redirect", classOf[RedirectActionViewRender])
    bind("mvc.TaglibraryProvider.default", classOf[ContainerTaglibraryProvider])
    bind("mvc.ViewRender.stream", classOf[StreamViewRender])
    bind("mvc.ViewRender.status", classOf[StatusViewRender])

    //dispatch
    bind("mvc.ActionUriRender.default", classOf[DefaultActionUriRender])
    bind("mvc.RequestMapper.default", classOf[HierarchicalUrlMapper])

    //execution
    bind("mvc.InvocationReactor.default", classOf[DefaultInvocationReactor])
    bind("web.Interceptor.flash", classOf[FlashInterceptor])
    bind("web.Interceptor.cors", classOf[CorsInterceptor])
    bind("mvc.HandlerBuilder.default", classOf[StaticMethodHandlerBuilder])
    bind("mvc.HandlerBuilder.method", classOf[DynaMethodHandlerBuilder])

    //context
    bind("mvc.TextResourceProvider.default", classOf[ActionTextResourceProvider])
    bind("mvc.TextFormater.default", classOf[DefaultTextFormater])
    bind("mvc.TextBundleRegistry.default", classOf[DefaultTextBundleRegistry])
    bind("mvc.LocaleResolver.default", classOf[ParamLocaleResolver])
    bind("mvc.SerializerManager.default", classOf[DefaultSerializerManager])

    bind("mvc.ActionLauncher", classOf[ActionLauncher])
  }
}

@profile("dev")
class DevModule extends AbstractBindModule {
  protected override def binding() {
    bind("mvc.ActionMappingBuilder.default", classOf[DefaultActionMappingBuilder]).property("viewScan", "false")
    bind("mvc.TextBundleRegistry.default", classOf[DefaultTextBundleRegistry]).property("reloadable", "true")
    bind("mvc.HandlerBuilder.method", classOf[DynaMethodHandlerBuilder]).primary
  }
}

class SecurityModule extends AbstractBindModule {
  protected override def binding() {
    bind("web.RequestConvertor.mvc", classOf[MvcRequestConvertor])
  }
}