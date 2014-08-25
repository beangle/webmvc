package org.beangle.webmvc.config

import scala.annotation.migration
import org.beangle.commons.http.HttpMethods.{ DELETE, GET, HEAD, POST, PUT }
import org.beangle.commons.lang.Strings.{ join, split }
import org.beangle.webmvc.api.action.{ ToClass, ToURL }
import org.beangle.webmvc.api.view.View
import java.lang.reflect.Method

trait Configurer {

  def getProfile(className: String): Profile

  def profiles: Seq[Profile]
}

/**
 * action config (namespace endwith /)
 */
class ActionConfig(val clazz: Class[_], val name: String, val views: Map[String, View], val profile: Profile) {
  var mappings: Map[String, ActionMapping] = Map.empty
}

object ActionMapping {
  final val DefaultMethod = "index"
  final val MethodParam = "_method"
  import org.beangle.commons.http.HttpMethods.{ DELETE, GET, HEAD, POST, PUT }
  final val HttpMethodMap = Map((GET, ""), (POST, ""), (PUT, "put"), (DELETE, "delete"), (HEAD, "head"))
  final val HttpMethods = Set("put", "delete", "head")
}

class ActionMapping(val httpMethod: String, val config: ActionConfig, val method: Method, val name: String,
  val params: Array[String], val urlParams: Map[Integer, String]) {

  def httpMethodMatches(requestMethod: String): Boolean = {
    if (null == httpMethod) requestMethod == GET || requestMethod == POST
    else requestMethod == httpMethod
  }

  def url = config.name + "/" + name

  def fill(paramMaps: collection.Map[String, Any]*): String = {
    if (urlParams.isEmpty) return url
    val parts = split(url, '/')
    urlParams foreach {
      case (index, name) =>
        val iter = paramMaps.iterator
        var value: Option[Any] = None
        while (iter.hasNext && value == None) {
          value = iter.next.get(name)
        }
        parts(index) = String.valueOf(value.get)
    }
    "/" + join(parts, "/")
  }

  override def toString: String = {
    (if (null == httpMethod) "*" else httpMethod) + " " + url + " " + config.clazz.getName + "." +
      method.getName + "(" + join(params, ",") + ")"
  }

  def toURL(paramMaps: collection.Map[String, Any]*): ToURL = {
    val ua = new ToURL(fill(paramMaps: _*))
    if (this.httpMethod != null) {
      val method = ActionMapping.HttpMethodMap(this.httpMethod)
      if ("" != method) ua.param(ActionMapping.MethodParam, method)
    }
    ua
  }
}
