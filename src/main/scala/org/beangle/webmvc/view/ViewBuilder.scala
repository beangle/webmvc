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

package org.beangle.webmvc.view

import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.{description, spi}
import org.beangle.webmvc.annotation.view

/**
 * Builder view from annotation
 */
@spi
trait ViewBuilder {
  def build(view: view, defaultType: String): View
}

@description("缺省视图构建者")
class DefaultViewBuilder(typeViewBuilders: List[TypeViewBuilder]) extends ViewBuilder {

  private val builders = typeViewBuilders.map(builder => (builder.supportViewType, builder)).toMap

  override def build(view: view, defaultType: String): View = {
    val viewType = if (Strings.isEmpty(view.`type`)) defaultType else view.`type`
    builders(viewType).build(view)
  }

}
