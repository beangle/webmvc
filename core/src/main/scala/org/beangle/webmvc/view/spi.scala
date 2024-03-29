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

package org.beangle.webmvc.view

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.lang.annotation.spi
import org.beangle.web.action.annotation.view
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.view.View
import org.beangle.webmvc.config.{Profile, RouteMapping}

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

  def exists(viewPath:String):Boolean
}

@spi
trait ViewRender {
  def supportViewClass: Class[_]
  def render(view: View, context: ActionContext): Unit
}
case class ViewResult(data: AnyRef, contentType: String)

@spi
trait ViewDecorator {
  def decorate(data: ViewResult, uri: String, context: ActionContext): ViewResult
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
