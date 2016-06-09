/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.webmvc.view.freemarker

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.view.{ LocatedView, TypeViewBuilder }

class FreemarkerView(val location: String) extends LocatedView

@description("Freemaker视图构建器")
class FreemarkerViewBuilder extends TypeViewBuilder {

  override def build(v: view): View = {
    new FreemarkerView(v.location)
  }

  override def supportViewType: String = {
    "freemarker"
  }
}
