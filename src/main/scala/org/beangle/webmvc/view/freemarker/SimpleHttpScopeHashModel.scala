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

import freemarker.template.{ObjectWrapper, SimpleHash, TemplateModel}
import jakarta.servlet.http.HttpServletRequest

/**
 * Just extract value from default scope and request(omit session/context)
 */
class SimpleHttpScopeHashModel(wrapper: ObjectWrapper, val request: HttpServletRequest) extends SimpleHash(wrapper) {

  override def get(key: String): TemplateModel = {
    // Lookup in page scope
    val model = super.get(key)
    if (model != null) {
      return model
    }

    // Lookup in request scope
    val obj = request.getAttribute(key)
    if (obj != null) {
      return wrap(obj)
    }
    // return wrapper's null object (probably null).
    wrap(null)
  }
}
