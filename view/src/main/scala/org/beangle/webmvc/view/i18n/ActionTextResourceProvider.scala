/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.view.i18n

import java.{ util => ju }

import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.text.i18n.{ TextBundleRegistry, TextFormater, TextResource, TextResourceProvider }
import org.beangle.webmvc.api.context.ActionContext

@description("基于Action的文本资源提供者")
class ActionTextResourceProvider(registry: TextBundleRegistry, formater: TextFormater)
    extends TextResourceProvider with Initializing {

  var defaults: String = "beangle,application"

  override def init(): Unit = {
    registry.addDefaults(Strings.split(defaults, ","): _*)
  }

  def getTextResource(locale: ju.Locale): TextResource = {
    new ActionTextResource(ActionContext.current, locale, registry, formater)
  }
}
