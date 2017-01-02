/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.webmvc.view.tag

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.view.impl.UIIdGenerator

class ComponentContext(val uriRender: ActionUriRender, val idGenerator: UIIdGenerator, val templateEngine: TemplateEngine) {

  private val themeStack = new ThemeStack()

  private val components = new collection.mutable.Stack[Component]

  /**
   * Finds the nearest ancestor of this component stack.
   */
  def find[T <: Component](clazz: Class[T]): T = {
    components.find { component => clazz == component.getClass } match {
      case Some(c) => c.asInstanceOf[T]
      case None => null.asInstanceOf[T]
    }
  }

  def pop(): Component = {
    val elem = components.pop()
    if (null != elem.theme) themeStack.pop()
    elem
  }

  def theme: Theme = {
    if (themeStack.isEmpty) Themes.Default
    else themeStack.peek
  }

  def push(component: Component): Unit = {
    components.push(component)
    if (null != component.theme) themeStack.push(Themes(component.theme))
  }
}