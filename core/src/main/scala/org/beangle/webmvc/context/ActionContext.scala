package org.beangle.webmvc.context

import java.{ util => ju }
import org.beangle.commons.lang.Locales
import org.beangle.commons.text.i18n.TextResource
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.route.ActionMapping
import org.beangle.commons.collection.Collections

object ContextHolder {
  val contexts = new ThreadLocal[ActionContext]
  def context: ActionContext = contexts.get()
}

object ActionContext {
//  val ActionMappingKey = "_action_mapping"
  val URLParamIndexes = "_url_param_indexes"
}

class ActionContext(val request: HttpServletRequest, var response: HttpServletResponse, val params: Map[String, Any]) {

  import ActionContext._
  var local = ParamLocaleResolver.resolve(request)

  var actionMapping: ActionMapping = _

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