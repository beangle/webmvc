package org.beangle.webmvc.view.freemarker

import org.beangle.commons.lang.annotation.description
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
      request.setAttribute(templateModelAttribute, model)
      model
    } else {
      existed
    }
  }
}