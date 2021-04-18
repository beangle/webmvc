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
package org.beangle.webmvc.view

import org.beangle.cdi.bind.BindModule
import org.beangle.commons.text.i18n.{DefaultTextBundleRegistry, DefaultTextFormater}
import org.beangle.template.freemarker.web.FreemarkerModelBuilder
import org.beangle.webmvc.DefaultModule.bind
import org.beangle.webmvc.view.freemarker._
import org.beangle.webmvc.view.i18n.{ActionTextResourceProvider, TextResourceInitializer}
import org.beangle.webmvc.view.tag.CoreTagLibrary
import org.beangle.webmvc.view.tag.freemarker.TagTemplateEngine

object DefaultModule extends BindModule {

  protected override def binding(): Unit = {
    //config
    bind("mvc.TagTemplateEngine.freemarker", classOf[TagTemplateEngine])
      .property("enableCache", !devEnabled)
    bind("mvc.Taglibrary.c", classOf[CoreTagLibrary])

    //template
    bind("mvc.FreemarkerConfigurer.default", classOf[WebFreemarkerConfigurer]).property("enableCache", !devEnabled)
    bind("mvc.TemplateResolver.freemarker", classOf[HierarchicalTemplateResolver])

    //view
    bind("mvc.ViewResolver.freemarker", classOf[FreemarkerViewResolver])
    bind("mvc.ViewRender.freemarker", classOf[FreemarkerViewRender])
    bind("mvc.TypeViewBuilder.freemarker", classOf[FreemarkerViewBuilder])
    bind("mvc.TagLibraryProvider.default", classOf[ContainerTaglibraryProvider])
    bind("mvc.FreemarkerModelBuilder", classOf[FreemarkerModelBuilder])

    //i18n
    bind("mvc.TextResourceProvider.default", classOf[ActionTextResourceProvider])
    bind("mvc.TextFormatter.default", classOf[DefaultTextFormater])
    bind("mvc.TextBundleRegistry.default", classOf[DefaultTextBundleRegistry]).property("reloadable", devEnabled)
    bind("mvc.ActionContextInitializer.text", classOf[TextResourceInitializer])
  }
}
