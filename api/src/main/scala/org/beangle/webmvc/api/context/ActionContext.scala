package org.beangle.webmvc.api.context

import java.{ util => ju }
import org.beangle.commons.collection.Collections
import org.beangle.commons.text.i18n.TextResource
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object ContextHolder {
  val contexts = new ThreadLocal[ActionContext]
  def context: ActionContext = contexts.get()
}

class ActionContext(val request: HttpServletRequest, var response: HttpServletResponse, val params: Map[String, Any]) {

  var locale: ju.Locale = _

  var textResource: TextResource = _

  var flashMap: Flash = _

  private val temp = new collection.mutable.HashMap[String, Any]

  def attribute(name: String, value: Any): Unit = {
    request.setAttribute(name, value)
  }

  def removeAttribute(names: String*) {
    names foreach { name =>
      request.removeAttribute(name)
    }
  }

  def attribute[T](name: String): T = {
    request.getAttribute(name).asInstanceOf[T]
  }

  def temp(name: String, value: Any): Unit = {
    temp.put(name, value)
  }

  def temp[T](name: String): T = {
    temp.get(name).orNull.asInstanceOf[T]
  }

  def flash: Flash = {
    if (null == flashMap) {
      val session = request.getSession()
      if (null != session) {
        val flashObj = session.getAttribute("flash")
        if (null != flashObj) flashMap = flashObj.asInstanceOf[Flash]
        else {
          flashMap = new Flash
          session.setAttribute("flash", flashMap)
        }
      }
    }
    flashMap
  }
}