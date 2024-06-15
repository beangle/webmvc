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

package org.beangle.webmvc.view.freemarker

import org.beangle.commons.lang.annotation.description
import org.beangle.template.freemarker.{ProfileTemplateLoader, Configurer as FreemarkerConfigurer}
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
    var found: Boolean = false
    val profile = configurer.getProfile(actionClass.getName)
    while {
      val buf = new StringBuilder
      buf.append(templatePathMapper.map(superClass.getName, viewName, profile))
      buf.append(suffix)
      ProfileTemplateLoader.getProfile match
        case None =>
          path = buf.toString
          found = exists(path)
        case Some(templateProfile) =>
          path = templateProfile + buf.toString
          found = exists(path)
          if (!found) {
            path = buf.toString
            found = exists(path)
          }
      superClass = superClass.getSuperclass
      !found && !superClass.equals(classOf[Object]) && !superClass.isPrimitive
    } do ()
    if (found) path else null
  }

  override def exists(viewPath: String): Boolean = {
    val freemarkerCfg = freemarkerConfigurer.config
    try {
      freemarkerCfg.getTemplate(viewPath)
      true
    } catch {
      case _: FileNotFoundException => false
      case _: IOException => true
    }
  }
}
