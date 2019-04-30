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

import java.io.Writer
import java.{ util => ju }

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.{ Chars, Strings }
import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.execution.Handler

import javax.servlet.http.HttpServletRequest

class UIBean(context: ComponentContext) extends Component(context) {

  var id: String = _

  var cssClass: String = _

  override def end(writer: Writer, body: String): Boolean = {
    evaluateParams()
    mergeTemplate(writer)
    false
  }

  @throws(classOf[Exception])
  protected final def mergeTemplate(writer: Writer): Unit = {
    val engine = context.templateEngine
    engine.render(context.theme.getTemplatePath(getClass, engine.suffix), writer, this)
  }

  /**
   * 获得对应的国际化信息
   *
   * @param text
   * @return 当第一个字符不是字母或者不包含.或者包含空格的均返回原有字符串
   */
  protected final def getText(text: String): String = getText(text, text)

  protected final def getText(text: String, defaultText: String): String = {
    if (Strings.isEmpty(text)) return defaultText
    if (!Chars.isAsciiAlpha(text.charAt(0))) return defaultText
    if (-1 == text.indexOf('.') || -1 < text.indexOf(' ')) return defaultText
    else {
      if (text.endsWith(".id")) {
        val key = Strings.substringBeforeLast(text, ".id")
        ActionContext.current.textProvider.get(key, defaultText)
      } else {
        ActionContext.current.textProvider.get(text, defaultText)
      }
    }
  }

  protected final def request: HttpServletRequest = {
    ActionContext.current.request
  }

  protected final def requestURI: String = request.getRequestURI()

  protected final def requestParameter(name: String): String = request.getParameter(name)

  protected def getValue(obj: Any, property: String): Any = {
    obj match {
      case null                      => null
      case map: collection.Map[_, _] => map.asInstanceOf[collection.Map[String, Any]].get(property).orNull
      case javaMap: ju.Map[_, _]     => javaMap.get(property)
      case o: AnyRef                 => Properties.get(o, property)
    }
  }

  protected final def render(uri: String): String = {
    context.uriRender.render(Handler.mapping, uri)
  }

  protected final def generateIdIfEmpty(): Unit = {
    if (Strings.isEmpty(id)) id = context.idGenerator.generate(getClass)
  }

  /**
   * Process label,convert empty to null
   */
  protected final def processLabel(label: String, name: String): String = {
    if (null != label) {
      if (Strings.isEmpty(label)) null else getText(label)
    } else getText(name)
  }
}

class ClosingUIBean(context: ComponentContext) extends UIBean(context) {
  var body: String = _

  override def start(writer: Writer): Boolean = {
    evaluateParams()
    true
  }

  override final def usesBody(): Boolean = true

  override final def end(writer: Writer, body: String): Boolean = {
    doEnd(writer, body)
  }

  def doEnd(writer: Writer, body: String): Boolean = {
    this.body = body
    mergeTemplate(writer)
    false
  }

}
class IterableUIBean(context: ComponentContext) extends ClosingUIBean(context) {

  protected def next(): Boolean = false

  protected def iterator(writer: Writer, body: String) {
    this.body = body
    mergeTemplate(writer)
  }

  override def start(writer: Writer): Boolean = {
    evaluateParams()
    next()
  }

  override def doEnd(writer: Writer, body: String): Boolean = {
    iterator(writer, body)
    next()
  }

}
