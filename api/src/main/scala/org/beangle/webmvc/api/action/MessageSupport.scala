package org.beangle.webmvc.api.action

import org.beangle.webmvc.api.context.Flash
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.api.context.ActionMessages
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.commons.lang.Chars

trait MessageSupport {

  final def getText(aTextName: String): String = ContextHolder.context.textResource(aTextName).get

  final def getText(key: String, defaultValue: String, args: Any*): String = ContextHolder.context.textResource(key, defaultValue, args: _*)

  protected final def getTextInternal(msgKey: String, args: Any*): String = {
    if (Strings.isEmpty(msgKey)) null
    if (Chars.isAsciiAlpha(msgKey.charAt(0)) && msgKey.indexOf('.') > 0) {
      getText(msgKey, msgKey, args: _*)
    } else {
      msgKey
    }
  }

  protected final def addMessage(msgKey: String, args: Any*): Unit = {
    ContextHolder.context.flash.addMessageNow(getTextInternal(msgKey, args: _*))
  }

  protected final def addError(msgKey: String, args: Any*): Unit = {
    ContextHolder.context.flash.addErrorNow(getTextInternal(msgKey, args: _*))
  }

  protected final def addFlashError(msgKey: String, args: Any*): Unit = {
    ContextHolder.context.flash.addError(getTextInternal(msgKey, args: _*))
  }

  protected final def addFlashMessage(msgKey: String, args: Any*): Unit = {
    ContextHolder.context.flash.addMessage(getTextInternal(msgKey, args: _*))
  }

  /**
   * 获得action消息<br>
   */
  @ignore
  protected final def actionMessages: List[String] = {
    val messages = ContextHolder.context.flash.get(Flash.MESSAGES).asInstanceOf[ActionMessages]
    if (null == messages) List()
    else messages.messages.toList
  }

  /**
   * 获得aciton错误消息<br>
   */
  @ignore
  protected final def actionErrors: List[String] = {
    val messages = ContextHolder.context.flash.get(Flash.MESSAGES).asInstanceOf[ActionMessages]
    if (null == messages) List()
    else messages.errors.toList
  }

}