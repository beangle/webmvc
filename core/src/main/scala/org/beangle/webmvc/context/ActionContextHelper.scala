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
package org.beangle.webmvc.context

import org.beangle.commons.collection.Collections
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.dispatch.RequestMapping
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.i18n.TextResourceProvider
import org.beangle.webmvc.api.context.ActionContextHolder
import org.beangle.commons.web.multipart.StandardMultipartResolver

object ActionContextHelper {

  private final val RequestMappingAttribute = "_request_mapping"

  def build(request: HttpServletRequest, response: HttpServletResponse, mapping: RequestMapping,
    localeResolver: LocaleResolver, textResourceProvider: TextResourceProvider,
    paramMaps: collection.Map[String, Any]*): ActionContext = {

    val params = new collection.mutable.HashMap[String, Any]
    val paramIter = request.getParameterMap.entrySet.iterator
    while (paramIter.hasNext) {
      val paramEntry = paramIter.next
      val values = paramEntry.getValue
      if (values.length == 1) params.put(paramEntry.getKey, values(0))
      else params.put(paramEntry.getKey, values)
    }

    if (StandardMultipartResolver.isMultipart(request)) {
      params ++= StandardMultipartResolver.resolve(request)
    }

    params ++= mapping.params
    paramMaps foreach (pMap => params ++= pMap)

    val context = new ActionContext(request, response, localeResolver.resolve(request), params.toMap)
    context.temp(RequestMappingAttribute, mapping)
    ActionContextHolder.contexts.set(context)
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