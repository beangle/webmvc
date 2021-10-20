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

import org.beangle.commons.text.i18n.TextProvider
import org.beangle.template.api.{ComponentContext, IndexableIdGenerator, TagTemplateEngine, TemplateEngine}
import org.beangle.web.action.context.ActionContext
import org.beangle.webmvc.context.{ActionContextInitializer, LocaleResolver}
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.view.i18n.ActionTextResourceProvider

class ComponentContextInitializer extends ActionContextInitializer {
  var textResourceProvider: ActionTextResourceProvider = _
  var localeResolver: LocaleResolver = _
  var uriRender: ActionUriRender = _
  var templateEngine: TagTemplateEngine = _

  override def init(context: ActionContext): Unit = {
    val locale = localeResolver.resolve(context.request)
    val textProvider = textResourceProvider.getTextResource(locale).asInstanceOf[TextProvider]

    val req = context.request
    val queryString = req.getQueryString
    val fullpath = if (null == queryString) req.getRequestURI else req.getRequestURI + queryString
    val idGenerator = new IndexableIdGenerator(String.valueOf(Math.abs(fullpath.hashCode)))
    val services = Map("uriRender" -> uriRender)
    val cc = new ComponentContext(templateEngine, idGenerator, textProvider, services)

    context.locale = locale
    context.textProvider = textProvider
    context.stash("_beangle_webmvc_component_context", cc)
  }
}
