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

package org.beangle.webmvc.context

import org.beangle.commons.lang.annotation.spi
import org.beangle.web.servlet.http.accept.ContentNegotiationManager
import org.beangle.webmvc.context.ActionContext.{AcceptTypeKey, LocalKey, TextResourceKey}
import org.beangle.webmvc.i18n.ActionTextResourceProvider

@spi
trait ActionContextProperty {
  def get(context: ActionContext): Any

  def name: String
}

/** 提供相应所需的Locale
 */
class LocaleContextProperty extends ActionContextProperty {
  var localeResolver: LocaleResolver = _

  override def get(context: ActionContext): Any = {
    localeResolver.resolve(context.request)
  }

  override def name: String = LocalKey
}

/** 提供国际化属性
 */
class TextResourceContextProperty extends ActionContextProperty {
  var textResourceProvider: ActionTextResourceProvider = _

  override def get(context: ActionContext): Any = {
    textResourceProvider.getTextResource(context.locale, context.handler)
  }

  override def name: String = TextResourceKey
}

/** 提供相应能接受的媒体类型(MediaType)
 */
class AcceptTypeContextProperty extends ActionContextProperty {

  var contentNegotiationManager: ContentNegotiationManager = _

  override def get(context: ActionContext): Any = {
    contentNegotiationManager.resolve(context.request)
  }

  override def name: String = AcceptTypeKey
}
