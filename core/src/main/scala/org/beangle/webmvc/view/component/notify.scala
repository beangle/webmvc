package org.beangle.webmvc.view.component

import org.beangle.webmvc.api.context.{ ActionMessages, ContextHolder, Flash }

class Messages(context: ComponentContext) extends UIBean(context) {
  var actionMessages: List[String] = null
  var actionErrors: List[String] = null

  var clear = "true"

  override def evaluateParams() {
    val messages = ContextHolder.context.flash.get(Flash.MESSAGES).asInstanceOf[ActionMessages]
    if (null != messages && (!messages.messages.isEmpty || !messages.errors.isEmpty)) {
      generateIdIfEmpty()

      actionMessages = messages.messages.toList
      actionErrors = messages.errors.toList
      if ("true".equals(clear)) {
        messages.clear()
      }
    }
  }

  def hasActionErrors: Boolean = !actionErrors.isEmpty

  def hasActionMessages: Boolean = !actionMessages.isEmpty

}

class Dialog(context: ComponentContext) extends ClosingUIBean(context) {
  var title: String = _
  var href: String = _
  var modal = "false"

  override def evaluateParams() {
    this.href = render(href);
  }
}