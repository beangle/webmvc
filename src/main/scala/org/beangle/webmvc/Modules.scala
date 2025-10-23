/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc

import org.beangle.commons.cdi.{BindModule, profile}
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.text.i18n.{DefaultTextBundleLoader, DefaultTextFormatter}
import org.beangle.web.servlet.http.accept.ContentNegotiationManagerFactory
import org.beangle.webmvc.config.{DefaultActionMappingBuilder, DefaultConfigurator, XmlProfileProvider}
import org.beangle.webmvc.context.{DefaultActionContextBuilder, ParamLocaleResolver}
import org.beangle.webmvc.dispatch.*
import org.beangle.webmvc.execution.DynaMethodInvokerBuilder
import org.beangle.webmvc.execution.interceptors.CorsInterceptor
import org.beangle.webmvc.i18n.ActionTextResourceProvider

object DefaultModule extends BindModule {

  protected override def binding(): Unit = {
    //config
    bind("mvc.ProfileProvider.default", classOf[XmlProfileProvider])
    bind("mvc.Configurator.default", classOf[DefaultConfigurator])
    bind("mvc.ActionMappingBuilder.default", classOf[DefaultActionMappingBuilder])

    //i18n
    bind("mvc.TextResourceProvider.default", classOf[ActionTextResourceProvider])
      .property("reloadable", devEnabled)
    bind("mvc.TextFormatter.default", classOf[DefaultTextFormatter])
    bind("mvc.TextBundleLoader.default", classOf[DefaultTextBundleLoader])
    //dispatch
    bind("mvc.ActionUriRender.default", classOf[DefaultActionUriRender])
    bind("mvc.RequestMapper.default", classOf[HierarchicalUrlMapper])
    bind("mvc.RouteProvider.default", classOf[DefaultRouteProvider])

    //execution
    bind("web.Interceptor.cors", classOf[CorsInterceptor])
    bind("mvc.InvokerBuilder.default", classOf[DynaMethodInvokerBuilder])

    //context
    bind("mvc.LocaleResolver.default", classOf[ParamLocaleResolver])
    bind("mvc.ActionContextBuilder.default", classOf[DefaultActionContextBuilder])

    //content
    bind(classOf[ContentNegotiationManagerFactory]).property("favorPathExtension", "true")
      .property("favorParameter", "true").property("parameterName", "format")
      .property("ignoreAcceptHeader", "true")

    //security
    bind("web.RequestConvertor.mvc", classOf[MvcRequestConvertor])

    bind("Serializer.bin", DefaultBinarySerializer)
    //static
    bind(classOf[StaticResourceRouteProvider])
  }
}

class ViewModule extends BindModule {

  protected override def binding(): Unit = {
    //view
    import org.beangle.webmvc.view.*
    bind("mvc.TemplatePathMapper.default", classOf[DefaultTemplatePathMapper])
    bind("mvc.ViewBuilder.default", classOf[DefaultViewBuilder])
    bind("mvc.TypeViewBuilder.chain", classOf[ForwardActionViewBuilder])
    bind("mvc.TypeViewBuilder.redirectAction", classOf[RedirectActionViewBuilder])
    bind("mvc.ViewRender.chain", classOf[ForwardActionViewRender])
    bind("mvc.ViewRender.redirectAction", classOf[RedirectActionViewRender])
    bind("mvc.ViewRender.stream", classOf[StreamViewRender])
    bind("mvc.ViewRender.status", classOf[StatusViewRender])
    bind("mvc.ViewRender.raw", classOf[RawViewRender])
    bind("mvc.ViewManager.default", classOf[DefaultViewManager])
    //tag
    if (ClassLoaders.get("org.beangle.template.api.TagLibraryProvider").nonEmpty) {
      import org.beangle.webmvc.view.tag.*
      bind("mvc.TagLibraryProvider.default", classOf[ContainerTagLibraryProvider])
      bind("mvc.ActionContextInitializer.component", classOf[ComponentContextInitializer])
    }
  }
}

@profile("dev")
class DevModule extends BindModule {
  protected override def binding(): Unit = {
    bind("mvc.ActionMappingBuilder.default", classOf[DefaultActionMappingBuilder]).property("viewScan", "false")
  }
}
