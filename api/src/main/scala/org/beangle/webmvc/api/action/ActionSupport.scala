package org.beangle.webmvc.api.action

import java.net.URL

import scala.reflect.ClassTag

import org.beangle.commons.lang.{ Chars, ClassLoaders, Strings }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.{ CookieUtils, RequestUtils }
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.{ ActionMessages, ContextHolder, Flash, Params }
import org.beangle.webmvc.api.view.{ ForwardActionView, RedirectActionView, View }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import java.sql
import java.{ util => ju }

object ActionSupport {
  val ERROR = "error"
  val INPUT = "input"
}

class ActionSupport extends Logging {

  protected final def forward(view: String = null): String = {
    view
  }

  protected final def forward(view: String, message: String): String = {
    addMessage(getText(message))
    view
  }

  protected final def forward(action: To): View = {
    new ForwardActionView(action)
  }

  protected final def forward(action: To, message: String): View = {
    if (Strings.isNotBlank(message)) {
      if (Strings.contains(message, "error")) addError(message)
      else addMessage(message)
    }
    new ForwardActionView(action)
  }

  @inline
  protected final def to(obj: Object, method: String): ToClass = {
    new ToClass(obj.getClass, method)
  }

  @inline
  protected final def to(obj: Object, method: String, params: String): ToClass = {
    new ToClass(obj.getClass, method).params(params)
  }

  @inline
  protected final def to(clazz: Class[_], method: String): ToClass = {
    new ToClass(clazz, method)
  }

  @inline
  protected final def to(clazz: Class[_], method: String, params: String): ToClass = {
    new ToClass(clazz, method).params(params)
  }

  @inline
  protected final def to(uri: String, params: String): ToURL = {
    new ToURL(uri).params(params)
  }

  @inline
  protected final def to(uri: String): ToURL = {
    To(uri)
  }

  protected final def redirect(method: String): View = {
    redirect(to(this, method), null)
  }

  protected final def redirect(method: String, message: String): View = {
    redirect(to(this, method), message)
  }

  protected final def redirect(method: String, params: String, message: String): View = {
    redirect(to(this, method, params), message)
  }

  protected final def redirect(action: To, message: String): View = {
    if (Strings.isNotEmpty(message)) addFlashMessage(message)
    new RedirectActionView(action)
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

  protected final def getAll[T: ClassTag](paramName: String, clazz: Class[T]) = Params.getAll(paramName, clazz)

  protected final def get(paramName: String) = Params.get(paramName)

  protected final def get[T](paramName: String, defaultValue: T): T = {
    val value = Params.get(paramName)
    if (value.isEmpty) defaultValue else Params.converter.convert(value.get, defaultValue.getClass).getOrElse(defaultValue)
  }

  protected final def getAttribute(name: String): Any = ContextHolder.context.attribute(name)

  protected final def getAttribute[T](name: String, clazz: Class[T]): T = {
    ContextHolder.context.attribute(name).asInstanceOf[T]
  }

  protected final def get[T](name: String, clazz: Class[T]): Option[T] = {
    Params.get(name, clazz)
  }

  protected final def getBoolean(name: String): Option[Boolean] = {
    Params.getBoolean(name)
  }

  protected final def getBoolean(name: String, defaultValue: Boolean): Boolean = {
    Params.getBoolean(name).getOrElse(defaultValue)
  }

  protected final def getDate(name: String): Option[sql.Date] = {
    Params.getDate(name)
  }

  protected final def getDateTime(name: String): Option[ju.Date] = {
    Params.getDateTime(name)
  }

  protected final def getFloat(name: String): Option[Float] = {
    Params.getFloat(name)
  }

  protected final def getShort(name: String): Option[Short] = {
    Params.getShort(name)
  }

  protected final def getInt(name: String): Option[Int] = {
    Params.getInt(name)
  }

  protected final def getInt(name: String, defaultValue: Int): Int = {
    Params.getInt(name).getOrElse(defaultValue)
  }

  protected final def getLong(name: String): Option[Long] = {
    Params.getLong(name)
  }

  protected final def getCookieValue(cookieName: String): String = {
    CookieUtils.getCookieValue(request, cookieName)
  }

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