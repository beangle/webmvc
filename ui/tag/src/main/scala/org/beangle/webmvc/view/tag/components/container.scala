package org.beangle.webmvc.view.tag.components

import org.beangle.commons.lang.{ Objects, Strings }
import org.beangle.webmvc.view.tag.ComponentContext

class Div(context: ComponentContext) extends ClosingUIBean(context) {
  var href: String = _

  var astarget: String = _

  override def evaluateParams() {
    if (null == astarget && (null != id || null != href)) astarget = "true"
    if (null != href) {
      generateIdIfEmpty()
      href = render(this.href)
    }
    if (!Objects.equals(astarget, "false")) {
      if (null != cssClass) cssClass = Strings.concat("ajax_container", " ", cssClass)
      else cssClass = "ajax_container"
    }
  }
}

class Iframe(context: ComponentContext) extends ClosingUIBean(context) {
  var src: String = _
  override def evaluateParams() {
    src = render(src)
  }
}

class Tab(context: ComponentContext) extends ClosingUIBean(context) {
  var href: String = _
  var label: String = _

  override def evaluateParams() {
    if (null != href) href = render(href)
    if (null != label) label = getText(label)
    val tabs = findAncestor(classOf[Tabs])
    if (Strings.isEmpty(id)) id = tabs.id + "_tab" + tabs.tabs.size
    tabs.addTab(this)
  }
}

class Tabs(context: ComponentContext) extends ClosingUIBean(context) {

  var selected: Int = 0
  val tabs = new collection.mutable.ListBuffer[Tab]

  def addTab(tab: Tab) {
    this.tabs += tab
  }

  override def evaluateParams() {
    generateIdIfEmpty()
  }

}

class Module(context: ComponentContext) extends ClosingUIBean(context) {
  var title: String = _

  override def evaluateParams() {
    generateIdIfEmpty()
    if (null != title) {
      title = getText(title)
    }
  }
}