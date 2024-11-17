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

package org.beangle.webmvc.view.freemarker

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.annotation.view
import org.beangle.webmvc.view.{TypeViewBuilder, View}

class FreemarkerView(val location: String) extends View

@description("Freemarker视图构建器")
class FreemarkerViewBuilder extends TypeViewBuilder {

  override def build(v: view): View = {
    new FreemarkerView(v.location)
  }

  override def supportViewType: String = {
    "freemarker"
  }
}
