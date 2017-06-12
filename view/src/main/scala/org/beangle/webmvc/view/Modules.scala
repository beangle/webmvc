/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.webmvc.view

import org.beangle.commons.text.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.commons.cdi.bind.{ BindModule, profile }
import org.beangle.webmvc.view.i18n.{ ActionTextResourceProvider, TextResourceInitializer }
import org.beangle.webmvc.view.tag.CoreTagLibrary
import org.beangle.webmvc.view.tag.freemarker.FreemarkerTemplateEngine
import org.beangle.webmvc.view.freemarker.FreemarkerViewBuilder
import org.beangle.webmvc.view.freemarker.WebFreemarkerConfigurer
import org.beangle.webmvc.view.freemarker.HierarchicalTemplateResolver
import org.beangle.webmvc.view.freemarker.FreemarkerViewResolver
import org.beangle.webmvc.view.freemarker.FreemarkerViewRender
import org.beangle.webmvc.view.freemarker.FreemarkerModelBuilder

object DefaultModule extends BindModule {

  protected override def binding() {
    //config
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine])
      .property("enableCache", !devEnabled)
    bind("mvc.Taglibrary.c", classOf[CoreTagLibrary])

    //template
    bind("mvc.FreemarkerConfigurer.default", classOf[WebFreemarkerConfigurer]).property("enableCache", !devEnabled)
    bind("mvc.TemplateResolver.freemarker", classOf[HierarchicalTemplateResolver])

    //view
    bind("mvc.ViewResolver.freemarker", classOf[FreemarkerViewResolver])
    bind("mvc.ViewRender.freemarker", classOf[FreemarkerViewRender])
    bind("mvc.TypeViewBuilder.freemarker", classOf[FreemarkerViewBuilder])
    bind("mvc.FreemarkerModelBuilder", classOf[FreemarkerModelBuilder])

    //i18n
    bind("mvc.TextResourceProvider.default", classOf[ActionTextResourceProvider])
    bind("mvc.TextFormater.default", classOf[DefaultTextFormater])
    bind("mvc.TextBundleRegistry.default", classOf[DefaultTextBundleRegistry]).property("reloadable", devEnabled)
    bind("mvc.ActionContextInitializer.text", classOf[TextResourceInitializer])
  }
}
