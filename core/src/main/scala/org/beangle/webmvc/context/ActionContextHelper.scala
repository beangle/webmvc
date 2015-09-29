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

import org.beangle.commons.i18n.TextResourceProvider
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.web.multipart.StandardMultipartResolver
import org.beangle.webmvc.api.context.{ ActionContext, ActionContextHolder }
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.dispatch.HandlerHolder
import org.beangle.webmvc.execution.MappingHandler
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.dispatch.HandlerHolder
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.execution.Handler

object ActionContextHelper {

  private final val HandlerHolderAttribute = "_handler_holder"

  def build(request: HttpServletRequest, response: HttpServletResponse, holder: HandlerHolder,
    localeResolver: LocaleResolver, textResourceProvider: Option[TextResourceProvider],
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

    params ++= holder.params
    paramMaps foreach (pMap => params ++= pMap)

    val context = new ActionContext(request, response, localeResolver.resolve(request), params.toMap)
    context.temp(HandlerHolderAttribute, holder)
    ActionContextHolder.contexts.set(context)
    
    textResourceProvider.foreach { trp =>
      context.textResource = trp.getTextResource(context.locale)
    }
    context
  }

  def handler: Handler = {
    ActionContextHolder.context.temp[HandlerHolder](HandlerHolderAttribute).handler
  }
  def mapping: RouteMapping = {
    handler.asInstanceOf[MappingHandler].mapping
  }
}