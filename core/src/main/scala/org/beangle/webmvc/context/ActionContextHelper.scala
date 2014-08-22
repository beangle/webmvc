package org.beangle.webmvc.context

import org.beangle.commons.collection.Collections
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.dispatch.RequestMapping
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.text.i18n.TextResourceProvider
import org.beangle.webmvc.api.context.ContextHolder

object ActionContextHelper {

  private final val RequestMappingAttribute = "_request_mapping"

  def build(request: HttpServletRequest, response: HttpServletResponse, mapping: RequestMapping, localeResolver: LocaleResolver, textResourceProvider: TextResourceProvider, paramMaps: collection.Map[String, Any]*): ActionContext = {
    val params = new collection.mutable.HashMap[String, Any]
    Collections.putAll(params, request.getParameterMap)
    params ++= mapping.params

    paramMaps foreach (pMap => params ++= pMap)

    val context = new ActionContext(request, response, localeResolver.resolve(request), params.toMap)
    context.temp(RequestMappingAttribute, mapping)
    ContextHolder.contexts.set(context)
    context.textResource = textResourceProvider.getTextResource(context.locale)
    context
  }

  def setMapping(context: ActionContext, mapping: RequestMapping): Unit = {
    context.temp(RequestMappingAttribute, mapping)
  }

  def getMapping(context: ActionContext): RequestMapping = {
    context.temp(RequestMappingAttribute)
  }
}