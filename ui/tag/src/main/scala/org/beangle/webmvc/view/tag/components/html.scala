package org.beangle.webmvc.view.tag.components

import java.io.Writer

import org.beangle.commons.http.agent.Browser
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.view.tag.{ ComponentContext, Themes }

class Head(context: ComponentContext) extends ClosingUIBean(context) {
  var loadui = true
  var compressed = true
  override def evaluateParams() {
    val devMode = requestParameter("devMode")
    if (null != devMode) compressed = !("true".equals(devMode) || "on".equals(devMode))
  }
}

class Foot(context: ComponentContext) extends ClosingUIBean(context)

/**
 * Useragent specific javascript
 */
class Agent(context: ComponentContext) extends UIBean(context) {

  private val tempBrowser = Browser.parse(request.getHeader("USER-AGENT"))
  val browser = tempBrowser.category.name
  val version = tempBrowser.version
}

class Css(context: ComponentContext) extends UIBean(context) {
  var href: String = _
}

object Anchor {

  val ReservedTargets = Set("_blank", "_top", "_self", "_parent", "new")
}

class Anchor(context: ComponentContext) extends ClosingUIBean(context) {
  var href: String = _
  var target: String = _
  var onclick: String = _

  def reserved: Boolean = Anchor.ReservedTargets.contains(target)

  override def evaluateParams() = {
    this.href = render(this.href)
    if (!reserved) {
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

  override def doEnd(writer: Writer, body: String): Boolean = {
    if (context.theme.name == Themes.Default) {
      try {
        writer.append("<a href=\"")
        writer.append(href).append("\"")
        if (null != id) {
          writer.append(" id=\"").append(id).append("\"")
        }
        if (null != target) {
          writer.append(" target=\"").append(target).append("\"")
        }
        if (null != onclick) {
          writer.append(" onclick=\"").append(onclick).append("\"")
        }
        writer.append(parameterString)
        writer.append(">").append(body).append("</a>")
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
      return false
    } else {
      return super.doEnd(writer, body)
    }
  }
}