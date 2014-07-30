package org.beangle.webmvc.context

import java.{ util => ju }

import org.beangle.commons.lang.Locales
import org.beangle.commons.text.i18n.TextResource

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

object ContextHolder {
  val contexts = new ThreadLocal[ActionContext]
  def context: ActionContext = contexts.get()
}

class ActionContext(val request: HttpServletRequest, val response: HttpServletResponse, val params: Map[String, Any]) {

  var local = ParamLocaleResolver.resolve(request)

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
    val session = request.getSession()
    if (null != session) {
      val flash = session.getAttribute("flash").asInstanceOf[Flash]
      if (null == flash) {
        val nflash = new Flash
        session.setAttribute("flash", nflash)
        nflash
      } else flash
    } else null
  }

  def textResource: TextResource = {
    attribute("textResource")
  }
}

