package org.beangle.webmvc.view.impl

import org.beangle.commons.web.context.ServletContextHolder
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.dispatch.ActionMapping
import org.beangle.webmvc.view.{ LocatedView, ViewPathMapper, ViewRender, ViewResolver }
import org.beangle.webmvc.view.freemarker.{ HierarchicalTemplateResolverByConfig, ParametersHashModel, ServletContextHashModel }

import freemarker.ext.servlet.{ AllHttpScopesHashModel, HttpRequestHashModel, HttpSessionHashModel }
import freemarker.template.{ ObjectWrapper, SimpleHash, Template }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class FreemarkerView(val location: String) extends LocatedView

class FreemarkerViewResolver(configurer: Configurer, freemarkerConfigurer: FreemarkerConfigurer, viewPathMapper: ViewPathMapper)
  extends ViewResolver with ViewRender {
  final val KEY_APPLICATION = "Application"
  final val KEY_SESSION = "Session"
  final val KEY_REQUEST = "Request"
  final val KEY_REQUEST_PARAMETERS = "Parameters"

  val templateResolver = new HierarchicalTemplateResolverByConfig(freemarkerConfigurer.config, viewPathMapper, configurer)

  val configuration = freemarkerConfigurer.config
  val servletContext = ServletContextHolder.context
  //FIXME migrate beangle freemark manager
  val servletContextModel = new ServletContextHashModel(servletContext, configuration.getObjectWrapper)

  def resolve(viewName: String, mapping: ActionMapping): View = {
    val clazz = mapping.clazz
    val suffix = configurer.getProfile(clazz.getName).viewSuffix
    val path = templateResolver.find(mapping.clazz, viewName, suffix)
    if (null == path) null else new FreemarkerView(path)
  }

  def render(view: View, context: ActionContext): Unit = {
    val freemarkerView = view.asInstanceOf[FreemarkerView]
    val template = configuration.getTemplate(freemarkerView.location, context.locale)
    val model = createModel(configuration.getObjectWrapper, context.request, context.response, context)
    processTemplate(template, model, context.response)
  }

  protected def processTemplate(template: Template, model: SimpleHash, response: HttpServletResponse): Unit = {
    val attrContentType = template.getCustomAttribute("content_type")
    if (attrContentType == null) response.setContentType(freemarkerConfigurer.contentType)
    else response.setContentType(attrContentType.toString)
    template.process(model, response.getWriter)
  }

  def supportView: Class[_] = {
    classOf[FreemarkerView]
  }

  protected def createModel(wrapper: ObjectWrapper, request: HttpServletRequest, response: HttpServletResponse, context: ActionContext): SimpleHash = {
    val model = new AllHttpScopesHashModel(wrapper, servletContext, request)
    model.put(KEY_APPLICATION, servletContextModel)
    val session = request.getSession(false)
    if (session != null) model.put(KEY_SESSION, new HttpSessionHashModel(session, wrapper))
    model.put(KEY_REQUEST, new HttpRequestHashModel(request, wrapper))
    model.put(KEY_REQUEST_PARAMETERS, new ParametersHashModel(context.params))
    for ((tagName, tag) <- freemarkerConfigurer.tags) {
      model.put(tagName.toString, tag.getModels(request, response))
    }
    model
  }

}