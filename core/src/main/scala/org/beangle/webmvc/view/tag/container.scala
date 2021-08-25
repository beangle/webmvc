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

import org.beangle.commons.lang.{Objects, Strings}
import org.beangle.template.api.{ComponentContext,UIBean,ClosingUIBean}

class Div(context: ComponentContext) extends ActionClosingUIBean(context) {
  var href: String = _

  var astarget: String = _

  override def evaluateParams(): Unit = {
    if (null == astarget && (null != id || null != href)) astarget = "true"
    if (null != href) {
      generateIdIfEmpty()
      href = render(this.href)
    }
    if (!Objects.equals(astarget, "false")) {
      addClass("ajax_container")
    }
  }
}

class Iframe(context: ComponentContext) extends ActionClosingUIBean(context) {
  var src: String = _

  override def evaluateParams(): Unit = {
    src = render(src)
  }
}

class Tab(context: ComponentContext) extends ActionClosingUIBean(context) {
  var href: String = _
  var label: String = _

  override def evaluateParams(): Unit = {
    if (null != href) href = render(href)
    label = getText(label)
    val tabs = findAncestor(classOf[Tabs])
    if (Strings.isEmpty(id)) id = tabs.id + "_tab" + tabs.tabs.size
    tabs.addTab(this)
  }
}

class Tabs(context: ComponentContext) extends ClosingUIBean(context) {

  var selected: String = "0"
  val tabs = new collection.mutable.ListBuffer[Tab]

  def addTab(tab: Tab): Unit = {
    this.tabs += tab
  }

  override def evaluateParams(): Unit = {
    generateIdIfEmpty()
  }

}

class Card(context: ComponentContext) extends ClosingUIBean(context) {

  override def evaluateParams(): Unit = {
    addClass("card")
  }
}

class CardHeader(context: ComponentContext) extends ClosingUIBean(context) {
  var closeable: String = "false"
  var minimal: String = "false"
  var title: String = _

  override def evaluateParams(): Unit = {
    title = getText(title)
    addClass("card-header")
  }
}

class CardTools(context: ComponentContext) extends ClosingUIBean(context) {

  override def evaluateParams(): Unit = {
    addClass("card-tools")
  }
}

class CardBody(context: ComponentContext) extends ClosingUIBean(context) {
  override def evaluateParams(): Unit = {
    addClass("card-body")
  }
}

class CardFooter(context: ComponentContext) extends ClosingUIBean(context) {
  override def evaluateParams(): Unit = {
    addClass("card-footer")
  }
}
