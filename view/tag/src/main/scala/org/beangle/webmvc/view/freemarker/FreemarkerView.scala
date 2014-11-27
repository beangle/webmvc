package org.beangle.webmvc.view.freemarker

import org.beangle.commons.lang.annotation.description
import org.beangle.commons.web.context.ServletContextHolder
import org.beangle.template.freemarker.{ FreemarkerConfigurer, ParametersHashModel }
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ ActionMapping, Configurer }
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

  def resolve(viewName: String, mapping: ActionMapping): View = {
    val config = mapping.config
    val path = templateResolver.resolve(config.clazz, viewName, config.profile.viewSuffix)
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