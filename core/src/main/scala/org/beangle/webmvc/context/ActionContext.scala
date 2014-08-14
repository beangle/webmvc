package org.beangle.webmvc.context

import java.{ util => ju }
import org.beangle.commons.collection.Collections
import org.beangle.commons.text.i18n.TextResource
import org.beangle.webmvc.route.RequestMapping
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.route.ActionMapping

object ContextHolder {
  val contexts = new ThreadLocal[ActionContext]
  def context: ActionContext = contexts.get()
}

class ActionContext(val request: HttpServletRequest, var response: HttpServletResponse, val params: Map[String, Any]) {

  var locale: ju.Locale = _

  var mapping: RequestMapping = _

  var textResource: TextResource = _

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

}

object ActionContextBuilder {
  def build(request: HttpServletRequest, response: HttpServletResponse, localeResolver: LocaleResolver, mapping: RequestMapping, paramMaps: collection.Map[String, Any]*): ActionContext = {
    val params = new collection.mutable.HashMap[String, Any]
    Collections.putAll(params, request.getParameterMap)
    params ++= mapping.params

    paramMaps foreach (pMap => params ++= pMap)

    val context = new ActionContext(request, response, params.toMap)
    context.mapping = mapping
    context.locale = localeResolver.resolve(request)
    context
  }
}