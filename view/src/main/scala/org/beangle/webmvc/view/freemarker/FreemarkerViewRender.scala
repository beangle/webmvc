package org.beangle.webmvc.view.freemarker

import org.beangle.commons.lang.annotation.description
import org.beangle.template.freemarker.FreemarkerConfigurer
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.view.ViewRender

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
    val attrContentType = template.getCustomAttribute("content_type").asInstanceOf[String]
    if (attrContentType == null) response.setContentType(configurer.contentType)
    else {
      if (!attrContentType.contains("charset")) response.setCharacterEncoding(config.getDefaultEncoding())
      response.setContentType(attrContentType.toString)
    }
    template.process(model, response.getWriter)
  }

  def supportViewClass: Class[_] = {
    classOf[FreemarkerView]
  }
}