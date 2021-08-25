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

import org.beangle.commons.lang.annotation.description
import org.beangle.template.freemarker.{Configurer => FreemarkerConfigurer}
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.view.{TemplatePathMapper, TemplateResolver}

import java.io.{FileNotFoundException, IOException}

/**
 * Find template in class hierarchy with configuration without caching.
 * It need a ViewPathMapper
 */
@description("参考类层级模板查找器")
class HierarchicalTemplateResolver(freemarkerConfigurer: FreemarkerConfigurer, templatePathMapper: TemplatePathMapper, configurer: Configurer) extends TemplateResolver {

  override def resolve(actionClass: Class[_], viewName: String, suffix: String): String = {
    var path: String = null
    var superClass = actionClass
    var source: Object = null
    val profile = configurer.getProfile(actionClass.getName)
    val configuration = freemarkerConfigurer.config
    while {
      val buf = new StringBuilder
      buf.append(templatePathMapper.map(superClass.getName, viewName, profile))
      buf.append(suffix)
      path = buf.toString
      try {
        source = configuration.getTemplate(path)
      } catch {
        case _: FileNotFoundException =>  //ignore
        case _: IOException => source = "error ftl"
      }
      superClass = superClass.getSuperclass
      null == source && !superClass.equals(classOf[Object]) && !superClass.isPrimitive
    } do ()
    if (null == source) null else path
  }
}
