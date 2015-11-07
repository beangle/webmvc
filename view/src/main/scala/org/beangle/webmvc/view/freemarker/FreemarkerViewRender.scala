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

import java.io.StringWriter

import org.beangle.commons.lang.annotation.description
import org.beangle.commons.web.util.RequestUtils
import org.beangle.template.freemarker.FreemarkerConfigurer
import org.beangle.webmvc.api.context.{ ActionContext, ActionContextHolder }
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.execution.Handler
import org.beangle.webmvc.view.{ ViewRender, ViewResult }

import freemarker.template.{ SimpleHash, Template }
import javax.servlet.http.HttpServletResponse

/**
 * @author chaostone
 */
@description("Freemaker视图渲染器")
class FreemarkerViewRender(configurer: FreemarkerConfigurer, modelBuilder: FreemarkerModelBuilder) extends ViewRender {

  val config = configurer.config

  def render(view: View, context: ActionContext): Unit = {
    val freemarkerView = view.asInstanceOf[FreemarkerView]
    val template = config.getTemplate(freemarkerView.location, context.locale)
    val model = modelBuilder.createModel(config.getObjectWrapper, context.request, context.response, context)
    processTemplate(template, model, context.response)
  }

  protected def processTemplate(template: Template, model: SimpleHash, response: HttpServletResponse): Unit = {
    var contentType = template.getCustomAttribute("content_type").asInstanceOf[String]
    if (contentType == null) contentType = configurer.contentType
    val decorators = Handler.mapping.action.profile.decorators
    if (decorators.isEmpty) {
      if (!contentType.contains("charset")) response.setCharacterEncoding(config.getDefaultEncoding)
      response.setContentType(contentType)
      template.process(model, response.getWriter)
    } else {
      val strWriter = new StringWriter(512)
      template.process(model, strWriter)
      val context = ActionContextHolder.context
      val uri = RequestUtils.getServletPath(context.request)
      var lastResult = ViewResult(strWriter.getBuffer, contentType)
      decorators foreach { decorator =>
        lastResult = decorator.decorate(lastResult, uri, context)
      }
      response.setContentType(lastResult.contentType)
      lastResult.data match {
        case s: StringBuffer => response.getOutputStream.write(s.toString.getBytes)
        case b: Array[Byte]  => response.getOutputStream.write(b)
        case _               =>
      }
    }
  }

  def supportViewClass: Class[_] = {
    classOf[FreemarkerView]
  }
}