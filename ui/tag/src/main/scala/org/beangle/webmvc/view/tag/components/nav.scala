package org.beangle.webmvc.view.tag.components

import org.beangle.commons.collection.page.Page
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.view.tag.ComponentContext

class Toolbar(context: ComponentContext) extends ClosingUIBean(context) {
  var title: String = _

  override def evaluateParams() {
    generateIdIfEmpty()
    if (null != title) {
      title = getText(title)
    }
  }
}

class Navbar(context: ComponentContext) extends ClosingUIBean(context) {
  var brand: String = _
}

class Navlist(context: ComponentContext) extends ClosingUIBean(context) {

  override def evaluateParams() {
    if (cssClass == null) cssClass = "nav navbar-nav"
    else if (!cssClass.startsWith("nav ")) cssClass = "nav " + cssClass
  }
}

class Navitem(context: ComponentContext) extends ClosingUIBean(context) {
  var href: String = _
  var onclick: String = _
  var target: String = _
  var active = false

  override def evaluateParams() {
    if (null != href) {
      this.href = render(this.href)
      if (!active) {
        val starts = requestURI.startsWith(this.href)
        if (starts) {
          val sub = requestURI.substring(this.href.length)
          active = (sub.length == 0 || sub == "/index")
        }
      }
    }
  }
}
class Pagebar(context: ComponentContext) extends UIBean(context) {
  var page: Page[_] = _
}