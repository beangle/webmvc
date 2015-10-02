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
package org.beangle.webmvc.view.freemarker

import org.beangle.commons.lang.annotation.description
import org.beangle.commons.web.context.ServletContextHolder
import org.beangle.template.freemarker.{ FreemarkerConfigurer, ParametersHashModel }
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ RouteMapping, Configurer }
import org.beangle.webmvc.view.{ LocatedView, TagLibraryProvider, TemplateResolver, TypeViewBuilder, ViewRender, ViewResolver }

import freemarker.ext.servlet.{ AllHttpScopesHashModel, HttpRequestHashModel, HttpSessionHashModel }
import freemarker.template.{ ObjectWrapper, SimpleHash, Template }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class FreemarkerView(val location: String) extends LocatedView

@description("Freemaker视图构建器")
class FreemarkerViewBuilder extends TypeViewBuilder {

  override def build(view: view): View = {
    new FreemarkerView(view.location)
  }

  override def supportViewType: String = {
    "freemarker"
  }
}

@description("Freemaker视图解析器")
class FreemarkerViewResolver(configurer: Configurer, freemarkerManager: FreemarkerManager) extends ViewResolver with ViewRender {

  var templateResolver: TemplateResolver = _

  val configuration = freemarkerManager.config

  def resolve(actionClass: Class[_], viewName: String, suffix: String): View = {
    val path = templateResolver.resolve(actionClass, viewName, suffix)
    if (null == path) null else new FreemarkerView(path)
  }

  def resolve(viewName: String, mapping: RouteMapping): View = {
    val action = mapping.action
    val path = templateResolver.resolve(action.clazz, viewName, action.profile.viewSuffix)
    if (null == path) null else new FreemarkerView(path)
  }

  def render(view: View, context: ActionContext): Unit = {
    val freemarkerView = view.asInstanceOf[FreemarkerView]
    val template = configuration.getTemplate(freemarkerView.location, context.locale)
    val model = freemarkerManager.createModel(configuration.getObjectWrapper, context.request, context.response, context)
    processTemplate(template, model, context.response)
  }

  protected def processTemplate(template: Template, model: SimpleHash, response: HttpServletResponse): Unit = {
    val attrContentType = template.getCustomAttribute("content_type").asInstanceOf[String]
    if (attrContentType == null) response.setContentType(freemarkerManager.contentType)
    else {
      if (!attrContentType.contains("charset")) response.setCharacterEncoding(configuration.getDefaultEncoding())
      response.setContentType(attrContentType.toString)
    }
    template.process(model, response.getWriter)
  }

  def supportViewClass: Class[_] = {
    classOf[FreemarkerView]
  }

  def supportViewType: String = {
    "freemarker"
  }
}