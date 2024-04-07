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

import org.beangle.commons.collection.page.Page
import org.beangle.template.api.ComponentContext
import org.beangle.template.api.{UIBean,ClosingUIBean}
class Toolbar(context: ComponentContext) extends ClosingUIBean(context) {
  var title: String = _

  override def evaluateParams(): Unit = {
    generateIdIfEmpty()
    title = getText(title)
  }
}

class Navbar(context: ComponentContext) extends ClosingUIBean(context) {
  var brand: String = _
}

class Nav(context: ComponentContext) extends ClosingUIBean(context) {

  override def evaluateParams(): Unit = {
    addClass("nav")
    generateIdIfEmpty()
  }
}

class Navitem(context: ComponentContext) extends ActionClosingUIBean(context) {
  var href: String = _
  var onclick: String = _
  var target: String = _
  var active = false

  override def evaluateParams(): Unit = {
    if (null != href) {
      this.href = render(this.href)
      if (!active) {
        val qIdx = this.href.indexOf("?")
        val thisHrefUri = if (qIdx == -1) this.href else this.href.substring(0, qIdx)
        active = thisHrefUri == request.getRequestURI || thisHrefUri == request.getRequestURI + "/index"
      }
    }
  }
}

class Pagebar(context: ComponentContext) extends UIBean(context) {
  var page: Page[_] = _
}
