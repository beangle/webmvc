/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc

import org.beangle.commons.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.commons.inject.bind.{ AbstractBindModule, profile }
import org.beangle.webmvc.config.impl.{ DefaultActionMappingBuilder, DefaultConfigurer, XmlProfileProvider }
import org.beangle.webmvc.context.ActionLauncher
import org.beangle.webmvc.context.impl.{ ActionTextResourceProvider, ContainerActionFinder, ParamLocaleResolver }
import org.beangle.webmvc.dispatch.impl.{ DefaultActionUriRender, DefaultRouteProvider, HierarchicalUrlMapper }
import org.beangle.webmvc.execution.impl.{ DynaMethodInvokerBuilder, MvcRequestConvertor, StaticMethodInvokerBuilder }
import org.beangle.webmvc.execution.interceptors.{ CorsInterceptor, FlashInterceptor }
import org.beangle.webmvc.view.impl.{ ContainerTaglibraryProvider, DefaultTemplatePathMapper, DefaultViewBuilder, ForwardActionViewBuilder, ForwardActionViewRender, RedirectActionViewBuilder, RedirectActionViewRender, StatusViewRender, StreamViewRender, ViewManager, ViewResolverRegistry }

object DefaultModule extends AbstractBindModule {

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
    bind("mvc.ViewManager", classOf[ViewManager])

    //dispatch
    bind("mvc.ActionUriRender.default", classOf[DefaultActionUriRender])
    bind("mvc.RequestMapper.default", classOf[HierarchicalUrlMapper])
    bind("mvc.RouteProvider.default",classOf[DefaultRouteProvider])

    //execution
    bind("web.Interceptor.flash", classOf[FlashInterceptor])
    bind("web.Interceptor.cors", classOf[CorsInterceptor])
    bind("mvc.InvokerBuilder.default", classOf[StaticMethodInvokerBuilder])
    bind("mvc.InvokerBuilder.method", classOf[DynaMethodInvokerBuilder])

    //context
    bind("mvc.TextResourceProvider.default", classOf[ActionTextResourceProvider])
    bind("mvc.TextFormater.default", classOf[DefaultTextFormater])
    bind("mvc.TextBundleRegistry.default", classOf[DefaultTextBundleRegistry])
    bind("mvc.LocaleResolver.default", classOf[ParamLocaleResolver])

    bind("mvc.ActionLauncher", classOf[ActionLauncher])
  }
}

@profile("dev")
class DevModule extends AbstractBindModule {
  protected override def binding() {
    bind("mvc.ActionMappingBuilder.default", classOf[DefaultActionMappingBuilder]).property("viewScan", "false")
    bind("mvc.TextBundleRegistry.default", classOf[DefaultTextBundleRegistry]).property("reloadable", "true")
    bind("mvc.HandlerInvoker.method", classOf[DynaMethodInvokerBuilder]).primary
  }
}

class SecurityModule extends AbstractBindModule {
  protected override def binding() {
    bind("web.RequestConvertor.mvc", classOf[MvcRequestConvertor])
  }
}