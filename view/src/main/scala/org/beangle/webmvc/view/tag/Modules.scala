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
package org.beangle.webmvc.view.tag

import org.beangle.commons.inject.bind.{ AbstractBindModule, profile }
import org.beangle.webmvc.view.freemarker.{ FreemarkerManager, FreemarkerViewBuilder, FreemarkerViewResolver, HierarchicalTemplateResolver }
import org.beangle.webmvc.view.tag.freemarker.FreemarkerTemplateEngine

object DefaultModule extends AbstractBindModule {

  protected override def binding() {
    //config
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine]).property("enableCache", $("mvc.template_engine.cache", "true"))
    bind("mvc.Taglibrary.c", classOf[CoreTagLibrary])

    //template
    bind("mvc.FreemarkerConfigurer.default", classOf[FreemarkerManager])
    bind("mvc.TemplateResolver.freemarker", classOf[HierarchicalTemplateResolver])

    //view
    bind("mvc.ViewResolver.freemarker", classOf[FreemarkerViewResolver])
    bind("mvc.TypeViewBuilder.freemarker", classOf[FreemarkerViewBuilder])
  }
}

@profile("dev")
class DevModule extends AbstractBindModule {
  protected override def binding() {
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine]).property("enableCache", "false")

    bind("mvc.FreemarkerConfigurer.default", classOf[FreemarkerManager]).property("enableCache", "false")
  }
}