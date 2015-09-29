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
package org.beangle.webmvc.view

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ RouteMapping, Profile }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

@spi
trait TagLibrary {
  def getModels(req: HttpServletRequest, res: HttpServletResponse): AnyRef
}

@spi
trait TagLibraryProvider {

  def tagLibraries: Map[String, TagLibrary]
}

@spi
trait TemplatePathMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def map(className: String, viewName: String, profile: Profile): String
}

@spi
trait TemplateResolver {
  def resolve(actionClass: Class[_], viewName: String, suffix: String): String
}

@spi
trait ViewRender {
  def supportViewClass: Class[_]
  def render(view: View, context: ActionContext)
}

/**
 * find view by mapping and viewName
 */
@spi
trait ViewResolver {
  def resolve(actionClass: Class[_], viewName: String, suffix: String): View
  def resolve(viewName: String, mapping: RouteMapping): View
  def supportViewType: String
}

@spi
trait TypeViewBuilder {
  def build(view: view): View
  def supportViewType: String
}

/**
 * Builder view from annotation
 */
@spi
trait ViewBuilder {
  def build(view: view, defaultType: String): View
}

trait LocatedView extends View {
  def location: String
}