package org.beangle.webmvc.view.component

import java.io.Writer

import org.beangle.commons.lang.{Chars, Strings}
import org.beangle.webmvc.context.ContextHolder

import javax.servlet.http.HttpServletRequest

//FIXME
object UIBean {
  private val NumberFormat = "{0,number,#.##}"
}

class UIBean(context: ComponentContext) extends Component(context) {
  import UIBean._

  var id: String = _

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
      return ContextHolder.context.textResource(text, defaultText)
    }
  }

  protected final def request: HttpServletRequest = {
    ContextHolder.context.request
  }

  protected final def requestURI: String = request.getRequestURI()

  protected final def requestParameter(name: String): String = request.getParameter(name)

  //  protected def getValue(obj: Object, property: String): Object = {
  //    stack.push(obj)
  //    try {
  //      val value = stack.findValue(property)
  //      if (value.isInstanceOf[Number]) { return MessageFormat.format(NumberFormat, value) }
  //      return value
  //    } finally {
  //      stack.pop()
  //    }
  //  }

  protected final def render(uri: String): String = {
    context.uriRender.render(request.getServletPath(), uri)
  }

  protected final def generateIdIfEmpty(): Unit = {
    if (Strings.isEmpty(id)) id = context.idGenerator.generate(getClass)
  }

  /**
   * Process label,convert empty to null
   */
  protected final def processLabel(label: String, name: String): String = {
    if (null != label) {
      if (Strings.isEmpty(label)) return null
      else return getText(label)
    } else return getText(name)
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
    val again = doEnd(writer, body)
    again
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