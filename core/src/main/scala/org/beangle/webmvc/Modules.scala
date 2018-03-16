/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc

import org.beangle.cdi.bind.{ BindModule, profile }
import org.beangle.webmvc.config.impl.{ DefaultActionMappingBuilder, DefaultConfigurer, XmlProfileProvider }
import org.beangle.webmvc.context.impl.{ ContainerActionFinder, DefaultActionContextBuilder, ParamLocaleResolver }
import org.beangle.webmvc.dispatch.impl.{ DefaultActionUriRender, DefaultRouteProvider, HierarchicalUrlMapper }
import org.beangle.webmvc.execution.impl.{ DynaMethodInvokerBuilder, MvcRequestConvertor, StaticMethodInvokerBuilder }
import org.beangle.webmvc.execution.interceptors.CorsInterceptor
import org.beangle.webmvc.view.impl.{ ContainerTaglibraryProvider, DefaultTemplatePathMapper, DefaultViewBuilder, ForwardActionViewBuilder, ForwardActionViewRender, RedirectActionViewBuilder, RedirectActionViewRender, StatusViewRender, StreamViewRender, ViewManager }
import org.beangle.commons.web.http.accept.ContentNegotiationManagerFactory
import org.beangle.commons.io.DefaultBinarySerializer

object DefaultModule extends BindModule {

  protected override def binding() {
    //config
    bind("mvc.ProfileProvider.default", classOf[XmlProfileProvider])
    bind("mvc.Configurer.default", classOf[DefaultConfigurer])
    bind("mvc.ActionMappingBuilder.default", classOf[DefaultActionMappingBuilder])

    //template
    bind("mvc.TemplatePathMapper.default", classOf[DefaultTemplatePathMapper])

    //view
    bind("mvc.ViewBuilder.default", classOf[DefaultViewBuilder])
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
    bind("mvc.RouteProvider.default", classOf[DefaultRouteProvider])

    //execution
    bind("web.Interceptor.cors", classOf[CorsInterceptor])
    bind("mvc.InvokerBuilder.default", classOf[StaticMethodInvokerBuilder])
    bind("mvc.InvokerBuilder.method", classOf[DynaMethodInvokerBuilder])

    //context
    bind("mvc.ActionContextBuilder.default", classOf[DefaultActionContextBuilder])
    bind("mvc.ActionFinder.default", classOf[ContainerActionFinder])
    bind("mvc.LocaleResolver.default", classOf[ParamLocaleResolver])

    //content
    bind(classOf[ContentNegotiationManagerFactory]).property("favorPathExtension", "true")
      .property("favorParameter", "true").property("parameterName", "format").property("ignoreAcceptHeader", "false")
    bind("Serializer.bin", DefaultBinarySerializer)
  }
}

@profile("dev")
class DevModule extends BindModule {
  protected override def binding() {
    bind("mvc.ActionMappingBuilder.default", classOf[DefaultActionMappingBuilder]).property("viewScan", "false")
    bind("mvc.HandlerInvoker.method", classOf[DynaMethodInvokerBuilder]).primary
  }
}

class SecurityModule extends BindModule {
  protected override def binding() {
    bind("web.RequestConvertor.mvc", classOf[MvcRequestConvertor])
  }
}
