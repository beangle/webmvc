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

package org.beangle.webmvc.freemarker

import org.beangle.cdi.bind.BindModule
import org.beangle.template.freemarker.DefaultTagTemplateEngine
import org.beangle.webmvc.view.tag.BeangleTagLibrary

object DefaultModule extends BindModule {

  protected override def binding(): Unit = {
    //view
    bind("mvc.Taglibrary.b", classOf[BeangleTagLibrary])
    //config
    bind("mvc.TagTemplateEngine.freemarker", classOf[DefaultTagTemplateEngine])
      .property("devMode", devEnabled)

    //template
    bind("mvc.FreemarkerConfigurer.default", classOf[ContextFreemarkerConfigurer])
      .property("devMode", devEnabled)
    bind("mvc.TemplateResolver.freemarker", classOf[HierarchicalTemplateResolver])
    bind("mvc.ViewResolver.freemarker", classOf[FreemarkerViewResolver])
    bind("mvc.ViewRender.freemarker", classOf[FreemarkerViewRender])
    bind("mvc.TypeViewBuilder.freemarker", classOf[FreemarkerViewBuilder])
    bind("mvc.FreemarkerModelBuilder", classOf[FreemarkerModelBuilder])
  }
}
