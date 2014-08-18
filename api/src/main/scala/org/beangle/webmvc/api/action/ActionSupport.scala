package org.beangle.webmvc.api.action

import java.net.URL

import scala.reflect.ClassTag

import org.beangle.commons.lang.{ Chars, ClassLoaders, Strings }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.{ CookieUtils, RequestUtils }
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.{ ActionMessages, ContextHolder, Flash, Params }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object ActionSupport {
  /**
   * The action execution was successful. Show result
   * view to the end user.
   */
  val SUCCESS = "success"

  /**
   * The action execution was a failure.
   * Show an error view, possibly asking the
   * user to retry entering data.
   */
  val ERROR = "error"

  /**
   * The action execution require more input
   * in order to succeed.
   */
  val INPUT = "input"

}

class ActionSupport extends Logging {

  protected final def forward(view: String = ActionSupport.SUCCESS) = view

  protected final def forward(view: String, message: String) = {
    addMessage(getText(message))
    view
  }

  protected final def forward(action: To): String = {
    ContextHolder.context.temp("dispatch_action", action)
    "chain:dispatch_action"
  }

  protected final def forward(action: To, message: String): String = {
    if (Strings.isNotBlank(message)) {
      if (Strings.contains(message, "error")) addError(message)
      else addMessage(message)
    }
    forward(action)
  }

  protected final def redirect(method: String, message: String, params: String): String = {
    redirect(to(this, method, params), message)
  }

  protected final def redirect(method: String): String = {
    redirect(to(this, method), null)
  }

  protected final def redirect(method: String, message: String): String = {
    redirect(to(this, method), message)
  }

  protected final def redirect(action: To, message: String): String = {
    if (Strings.isNotEmpty(message)) addFlashMessage(message)
    ContextHolder.context.temp("dispatch_action", action)
    "redirectAction:dispatch_action"
  }

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

  @ignore
  protected def remoteAddr: String = RequestUtils.getIpAddr(request)

  protected final def put(key: String, value: Any) {
    ContextHolder.context.attribute(key, value)
  }

  protected final def getAll(paramName: String) = Params.getAll(paramName)

  protected final def getAll[T >: Any: ClassTag](paramName: String, clazz: Class[T]) = Params.getAll(paramName, clazz)

  protected final def get(paramName: String) = Params.get(paramName)

  protected final def get[T](paramName: String, defaultValue: T): T = {
    val value = Params.get(paramName)
    if (value.isEmpty) defaultValue else Params.converter.convert(value.get, defaultValue.getClass())
  }

  protected final def getAttribute(name: String): Any = ContextHolder.context.attribute(name)

  protected final def getAttribute[T](name: String, clazz: Class[T]): T = ContextHolder.context.attribute(name).asInstanceOf[T]

  protected final def get[T](name: String, clazz: Class[T]) = Params.get(name, clazz)

  protected final def getBoolean(name: String) = Params.getBoolean(name)

  protected final def getDate(name: String) = Params.getDate(name)

  protected final def getDateTime(name: String) = Params.getDateTime(name)

  protected final def getFloat(name: String) = Params.getFloat(name)

  protected final def getShort(name: String) = Params.getShort(name)

  protected final def getInt(name: String) = Params.getInt(name)

  protected final def getLong(name: String) = Params.getLong(name)

  protected final def getCookieValue(cookieName: String): String = CookieUtils.getCookieValue(request, cookieName)

  protected final def addCookie(name: String, value: String, path: String, age: Int) {
    try {
      CookieUtils.addCookie(request, response, name, value, path, age)
    } catch {
      case e: Exception => error("setCookie error", e)
    }
  }

  protected final def addCookie(name: String, value: String, age: Int) {
    try {
      CookieUtils.addCookie(request, response, name,
        value, age)
    } catch {
      case e: Exception => error("setCookie error", e)
    }
  }

  protected final def deleteCookie(name: String) {
    CookieUtils.deleteCookieByName(request, response, name)
  }

  protected final def getResource(name: String): URL = {
    val url = ClassLoaders.getResource(name)
    if (url == null) error(s"Cannot load template $name")
    url
  }

  @ignore
  protected final def request: HttpServletRequest = ContextHolder.context.request

  @ignore
  protected final def response: HttpServletResponse = ContextHolder.context.response

}