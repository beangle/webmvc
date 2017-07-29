/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
import org.beangle.commons.lang.SystemInfo
import org.beangle.template.freemarker.ParametersHashModel
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.view.TagLibraryProvider

import freemarker.template.{ ObjectWrapper, SimpleHash }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

/**
 * @author chaostone
 */
@description("Freemaker模型构建器")
class FreemarkerModelBuilder(tagLibraryProvider: TagLibraryProvider) {
  final val KEY_REQUEST_PARAMETERS = "Parameters"
  final val templateModelAttribute = ".freemarker.TemplateModel"

  def createModel(wrapper: ObjectWrapper, request: HttpServletRequest, response: HttpServletResponse, context: ActionContext): SimpleHash = {
    val existed = request.getAttribute(templateModelAttribute).asInstanceOf[SimpleHash]
    if (null == existed) {
      val model = new SimpleHttpScopesHashModel(wrapper, request)
      model.put("request", request)
      model.put(KEY_REQUEST_PARAMETERS, new ParametersHashModel(context.params, wrapper))
      tagLibraryProvider.tagLibraries foreach {
        case (tagName, tag) =>
          model.put(tagName.toString, tag.getModels(request, response))
      }
      model.put("base", request.getContextPath)
      SystemInfo.properties.get("beangle.webmvc.static_base") foreach { p =>
        model.put("static_base", p)
      }
      request.setAttribute(templateModelAttribute, model)
      model
    } else {
      existed
    }
  }
}
