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
  var title: String = _
  /** 是有已经有标签卡被选中了 */
  var selected = false
  var uri = {
    val sb = new StringBuilder(Strings.substringBeforeLast(requestURI, "."))
    if (-1 == sb.lastIndexOf("!")) sb.append("!index")
    sb.toString()
  }

  def isSelected(givenUri: String): Boolean = {
    if (selected) return false
    else {
      selected = sameAction(givenUri)
      return selected
    }
  }

  /**
   * 去除后缀比较是否是同一个resource(action!method)
   */
  private def sameAction(path: String): Boolean = {
    val firstSb = new StringBuilder(Strings.substringBefore(path, "."))
    if (-1 == firstSb.lastIndexOf("!")) firstSb.append("!index")
    firstSb.toString().equals(uri)
  }
}

class Navitem(context: ComponentContext) extends ClosingUIBean(context) {
  var href: String = _
  var onclick: String = _
  var target: String = _
  var selected = false

  override def evaluateParams() {
    if (null != href) {
      this.href = render(this.href)
      this.selected = findAncestor(classOf[Navbar]).isSelected(this.href)
    }
    if (null == onclick) {
      if (null != target) {
        onclick = Strings.concat("return bg.Go(this,'", target, "')")
        target = null
      } else {
        onclick = "return bg.Go(this,null)"
      }
    }
  }
}
class Pagebar(context: ComponentContext) extends UIBean(context) {
  var page: Page[_] = _
}