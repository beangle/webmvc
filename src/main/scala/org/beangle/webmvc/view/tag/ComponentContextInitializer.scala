/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.view.tag

import org.beangle.template.api.{ComponentContext, IndexableIdGenerator, TagTemplateEngine}
import org.beangle.webmvc.context.{ActionContext, ActionContextInitializer, LocaleResolver}
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.i18n.ActionTextResourceProvider

class ComponentContextInitializer extends ActionContextInitializer {
  var textResourceProvider: ActionTextResourceProvider = _
  var localeResolver: LocaleResolver = _
  var uriRender: ActionUriRender = _
  var templateEngine: TagTemplateEngine = _

  override def init(context: ActionContext): Unit = {
    val locale = localeResolver.resolve(context.request)
    val textResource = textResourceProvider.getTextResource(locale, context.handler)

    val req = context.request
    val queryString = req.getQueryString
    val fullpath = if (null == queryString) req.getRequestURI else req.getRequestURI + queryString
    val idGenerator = new IndexableIdGenerator(String.valueOf(Math.abs(fullpath.hashCode)))
    val services = Map("uriRender" -> uriRender)
    val cc = new ComponentContext(templateEngine, idGenerator, textResource, services)

    context.locale = locale
    context.textResource = textResource
    context.stash("_beangle_webmvc_component_context", cc)
  }
}
