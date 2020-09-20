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
package org.beangle.webmvc.view.tag

import java.{util => ju}

import jakarta.servlet.http.HttpServletRequest
import org.beangle.webmvc.view.tag.freemarker.TagModel

/**
 * New taglibrary.
 */
abstract class AbstractModels(val context: ComponentContext, request: HttpServletRequest) {

  val models = new ju.HashMap[Class[_], TagModel]

  protected def get(clazz: Class[_ <: Component]): TagModel = {
    var model = models.get(clazz)
    if (null == model) {
      model = new TagModel(context, clazz)
      models.put(clazz, model)
    }
    model
  }
}
