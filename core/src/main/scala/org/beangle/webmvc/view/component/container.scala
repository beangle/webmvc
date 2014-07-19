package org.beangle.webmvc.view.component

import java.io.Writer

import org.beangle.commons.lang.{ Objects, Strings }

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
      var className = "ajax_container"
      if (null != parameters.get("class")) {
        className = Strings.concat(className, " ", parameters.get("class").toString())
      }
      parameters.put("class", className)
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
  var target: String = _
  var label: String = _

  override def evaluateParams() {
    if (null != href) href = render(href)
    if (null != label) label = getText(label)
    generateIdIfEmpty()
    val tabs = findAncestor(classOf[Tabs])
    if (null != tabs) tabs.addTab(this)
  }

  override def doEnd(writer: Writer, body: String): Boolean = {
    if (null == target) {
      this.target = id + "_target"
      super.doEnd(writer, body)
    } else {
      false
    }
  }
}

class Tabs(context: ComponentContext) extends ClosingUIBean(context) {
  
  var selectedTab: String = _
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