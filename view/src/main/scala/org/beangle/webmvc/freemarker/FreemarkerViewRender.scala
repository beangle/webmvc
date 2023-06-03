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

import freemarker.template.{SimpleHash, Template}
import jakarta.servlet.http.HttpServletResponse
import org.beangle.commons.lang.annotation.description
import org.beangle.template.api.ModelBuilder
import org.beangle.template.freemarker.Configurer as FreemarkerConfigurer
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.view.View
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.execution.MappingHandler
import org.beangle.webmvc.view.{ViewRender, ViewResult}

import java.io.StringWriter

/** Freemarker视图渲染器
  *
  * @author chaostone
  */
@description("Freemarker视图渲染器")
class FreemarkerViewRender(configurer: FreemarkerConfigurer, modelBuilder: ModelBuilder) extends ViewRender {

  private val config = configurer.config

  def render(view: View, context: ActionContext): Unit = {
    val freemarkerView = view.asInstanceOf[FreemarkerView]
    val template = config.getTemplate(freemarkerView.location, context.locale)
    val model = modelBuilder.createModel(config).asInstanceOf[SimpleHash]
    try {
      processTemplate(template, model, context.response)
    } finally {
      configurer.cleanProfile()
    }
  }

  protected def processTemplate(template: Template, model: SimpleHash, response: HttpServletResponse): Unit = {
    var contentType = template.getCustomAttribute("content_type").asInstanceOf[String]
    if (contentType == null) contentType = configurer.contentType
    val mapping = ActionContext.current.handler.asInstanceOf[MappingHandler].mapping
    val decorators = mapping.action.profile.decorators
    if (decorators.isEmpty) {
      if (!contentType.contains("charset")) response.setCharacterEncoding(config.getDefaultEncoding)
      response.setContentType(contentType)
      template.process(model, response.getWriter)
    } else {
      val strWriter = new StringWriter(512)
      template.process(model, strWriter)
      val context = ActionContext.current
      val uri = RequestUtils.getServletPath(context.request)
      var lastResult = ViewResult(strWriter.getBuffer, contentType)
      decorators foreach { decorator =>
        lastResult = decorator.decorate(lastResult, uri, context)
      }
      response.setContentType(lastResult.contentType)
      lastResult.data match {
        case s: StringBuffer => response.getOutputStream.write(s.toString.getBytes)
        case b: Array[Byte] => response.getOutputStream.write(b)
        case _ =>
      }
    }
  }

  def supportViewClass: Class[_] = {
    classOf[FreemarkerView]
  }
}
