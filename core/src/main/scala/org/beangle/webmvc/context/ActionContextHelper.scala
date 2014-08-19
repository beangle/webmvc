package org.beangle.webmvc.context

import org.beangle.commons.collection.Collections
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.context.LocaleResolver
import org.beangle.webmvc.dispatch.RequestMapping

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object ActionContextHelper {

  private final val RequestMappingAttribute = "_request_mapping"

  def build(request: HttpServletRequest, response: HttpServletResponse, localeResolver: LocaleResolver, mapping: RequestMapping, paramMaps: collection.Map[String, Any]*): ActionContext = {
    val params = new collection.mutable.HashMap[String, Any]
    Collections.putAll(params, request.getParameterMap)
    params ++= mapping.params

    paramMaps foreach (pMap => params ++= pMap)

    val context = new ActionContext(request, response, params.toMap)
    context.temp(RequestMappingAttribute, mapping)
    context.locale = localeResolver.resolve(request)
    context
  }

  def setMapping(context: ActionContext, mapping: RequestMapping): Unit = {
    context.temp(RequestMappingAttribute, mapping)
  }

  def getMapping(context: ActionContext): RequestMapping = {
    context.temp(RequestMappingAttribute)
  }
}