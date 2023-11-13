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

package org.beangle.webmvc.view.i18n

import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.text.i18n.*
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.execution.Handler
import org.beangle.webmvc.execution.MappingHandler

import java.util as ju

@description("基于Action的文本资源提供者")
class ActionTextResourceProvider(loader: TextBundleLoader, formatter: TextFormatter)
  extends TextResourceProvider with Initializing {

  var reloadable: Boolean = _
  var defaults: String = "beangle,application"
  private val registry = new DefaultTextBundleRegistry
  private val textCache = new ActionTextCache

  override def init(): Unit = {
    registry.addDefaults(Strings.split(defaults, ",").toIndexedSeq: _*)
    registry.loader = this.loader
  }

  override def getTextResource(locale: ju.Locale, handler: Handler): TextResource = {
    if reloadable then
      val newRegistry = new DefaultTextBundleRegistry
      newRegistry.loader = this.loader
      newRegistry.addDefaults(Strings.split(defaults, ",").toIndexedSeq: _*)
      newResource(locale, newRegistry, handler, null)
    else
      newResource(locale, registry, handler, textCache)
  }

  def newResource(locale: ju.Locale, registry: TextBundleRegistry, handler: Handler, cache: ActionTextCache): TextResource = {
    handler match
      case mh: MappingHandler => new ActionTextResource(ActionContext.current, mh.mapping.action, locale, registry, formatter, cache)
      case _ => new DefaultTextResource(locale, registry, formatter)
  }
}
