package org.beangle.webmvc.view.impl

import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.web.context.ServletContextHolder
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ ActionMapping, Configurer }
import org.beangle.webmvc.view.{ LocatedView, TypeViewBuilder, ViewRender, ViewResolver }
import org.beangle.webmvc.view.freemarker.{ ParametersHashModel, ServletContextHashModel }
import org.beangle.webmvc.view.tag.TagLibraryProvider
import org.beangle.webmvc.view.template.TemplateResolver

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
class FreemarkerViewResolver(configurer: Configurer, freemarkerConfigurer: FreemarkerConfigurer, tagLibraryProvider: TagLibraryProvider)
  extends ViewResolver with ViewRender {
  final val KEY_APPLICATION = "Application"
  final val KEY_SESSION = "Session"
  final val KEY_REQUEST = "Request"
  final val KEY_REQUEST_PARAMETERS = "Parameters"

  var templateResolver: TemplateResolver = _

  val configuration = freemarkerConfigurer.config
  val servletContext = ServletContextHolder.context
  val servletContextModel = new ServletContextHashModel(servletContext, configuration.getObjectWrapper)

  def resolve(viewName: String, mapping: ActionMapping): View = {
    val config = mapping.config
    val path = templateResolver.resolve(config.clazz, viewName, config.profile.viewSuffix)
    if (null == path) null else new FreemarkerView(path)
  }

  def render(view: View, context: ActionContext): Unit = {
    val freemarkerView = view.asInstanceOf[FreemarkerView]
    val template = configuration.getTemplate(freemarkerView.location, context.locale)
    val model = createModel(configuration.getObjectWrapper, context.request, context.response, context)
    processTemplate(template, model, context.response)
  }

  protected def processTemplate(template: Template, model: SimpleHash, response: HttpServletResponse): Unit = {
    val attrContentType = template.getCustomAttribute("content_type").asInstanceOf[String]
    if (attrContentType == null) response.setContentType(freemarkerConfigurer.contentType)
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

  protected def createModel(wrapper: ObjectWrapper, request: HttpServletRequest, response: HttpServletResponse, context: ActionContext): SimpleHash = {
    val model = new AllHttpScopesHashModel(wrapper, servletContext, request)
    model.put(KEY_APPLICATION, servletContextModel)
    val session = request.getSession(false)
    if (session != null) model.put(KEY_SESSION, new HttpSessionHashModel(session, wrapper))
    model.put(KEY_REQUEST, new HttpRequestHashModel(request, wrapper))
    model.put(KEY_REQUEST_PARAMETERS, new ParametersHashModel(context.params))
    tagLibraryProvider.tagLibraries foreach {
      case (tagName, tag) =>
        model.put(tagName.toString, tag.getModels(request, response))
    }
    model
  }

}