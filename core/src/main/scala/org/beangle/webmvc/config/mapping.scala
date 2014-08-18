package org.beangle.webmvc.config

import java.lang.reflect.Method

import org.beangle.commons.http.HttpMethods.{ DELETE, GET, HEAD, POST, PUT }
import org.beangle.commons.lang.Strings.{ join, split }
import org.beangle.webmvc.api.action.{ ToClass, ToURI }

object ActionMapping {
  final val DefaultMethod = "index"
  final val MethodParam = "_method"
  import org.beangle.commons.http.HttpMethods.{ DELETE, GET, HEAD, POST, PUT }
  final val HttpMethodMap = Map((GET, ""), (POST, ""), (PUT, "put"), (DELETE, "delete"), (HEAD, "head"))
  final val HttpMethods = Set("put", "delete", "head")
}

class ActionMapping(val httpMethod: String, val url: String, val clazz: Class[_], val method: String, val paramNames: Array[String], val urlParamNames: Map[Integer, String], val namespace: String, val name: String) {
  val isPattern = url.contains("{") || url.contains("*")

  def httpMethodMatches(requestMethod: String): Boolean = {
    if (null == httpMethod) requestMethod == GET || requestMethod == POST
    else requestMethod == httpMethod
  }
  def fill(params: collection.Map[String, Any]): String = {
    if (!isPattern) return url
    val result = url
    val parts = split(url, '/')
    urlParamNames foreach {
      case (index, name) =>
        parts(index) = String.valueOf(params(name))
    }
    "/" + join(parts, "/")
  }

  override def toString: String = {
    (if (null == httpMethod) "*" else httpMethod) + " " + url + " " + clazz.getName + "." +
      method + "(" + join(paramNames, ",") + ") " + namespace + "(" + name + ")"
  }

  def toURI(ca: ToClass, params: Map[String, Any]): ToURI = {
    val ua = new ToURI(fill(params ++ ca.parameters))
    ca.parameters --= this.urlParamNames.values
    ua.params(ca.parameters)
    if (this.httpMethod != null) {
      val method = ActionMapping.HttpMethodMap(this.httpMethod)
      if ("" != method) ua.param(ActionMapping.MethodParam, method)
    }
    ua
  }
}

trait ActionMappingBuilder {

  def build(clazz: Class[_], profile: Profile): Seq[Tuple2[ActionMapping, Method]]
}