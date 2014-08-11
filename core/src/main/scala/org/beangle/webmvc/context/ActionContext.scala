package org.beangle.webmvc.context

import org.beangle.commons.collection.Collections
import org.beangle.commons.text.i18n.TextResource
import org.beangle.webmvc.route.RequestMapping

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

object ContextHolder {
  val contexts = new ThreadLocal[ActionContext]
  def context: ActionContext = contexts.get()
}

object ActionContext {
  val URLParams = "_url_params"
}

class ActionContext(val request: HttpServletRequest, var response: HttpServletResponse, val params: Map[String, Any]) {

  import ActionContext._
  var local = ParamLocaleResolver.resolve(request)

  var mapping: RequestMapping = _

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

object ActionContextBuilder {
  def build(request: HttpServletRequest, response: HttpServletResponse, paramMaps: collection.Map[String, Any]*): ActionContext = {
    val params = new collection.mutable.HashMap[String, Any]
    Collections.putAll(params, request.getParameterMap)
    paramMaps foreach (pMap => params ++= pMap)
    new ActionContext(request, response, params.toMap)
  }
}