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
import org.beangle.web.action.view.View
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.view.{TemplateResolver, ViewResolver}

import java.io.{FileNotFoundException, IOException}

/**
 * @author chaostone
 */
@description("Freemarker视图解析器")
class FreemarkerViewResolver(templateResolver: TemplateResolver) extends ViewResolver {

  def resolve(actionClass: Class[_], viewName: String, suffix: String): View = {
    if viewName.charAt(0) == '/' then
      load(viewName + suffix)
    else
      val path = templateResolver.resolve(actionClass, viewName, suffix)
      if (null == path) null else new FreemarkerView(path)
  }

  def resolve(viewName: String, mapping: RouteMapping): View = {
    val action = mapping.action
    if viewName.charAt(0) == '/' then
      load(viewName + action.profile.viewSuffix)
    else
      val path = templateResolver.resolve(action.clazz, viewName, action.profile.viewSuffix)
      if (null == path) null else new FreemarkerView(path)
  }

  private def load(path: String): FreemarkerView = {
    if templateResolver.exists(path) then new FreemarkerView(path) else null
  }

  def supportViewType: String = "freemarker"
}
