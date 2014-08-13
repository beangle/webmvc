package org.beangle.webmvc.route

import java.net.URLEncoder
import org.beangle.commons.lang.{ Objects, Strings }
import java.lang.reflect.Method

object Action {
  def apply(clazz: Class[_], method: String = "index"): ClassAction = {
    new ClassAction(clazz, method)
  }

  def apply(obj: Object, method: String): ClassAction = {
    new ClassAction(obj.getClass, method)
  }

  def apply(clazz: Class[_], method: String, params: String): ClassAction = {
    new ClassAction(clazz, method).params(params)
  }

  def apply(obj: Object, method: String, params: String): ClassAction = {
    new ClassAction(obj.getClass, method).params(params)
  }

  def apply(uri: String, params: String): URIAction = {
    new URIAction(uri).params(params)
  }

  def apply(uri: String): URIAction = {
    new URIAction(uri)
  }

  def toURIAction(ca: ClassAction, mapping: ActionMapping, params: Map[String, Any]): URIAction = {
    val ua = new URIAction(mapping.fill(params ++ ca.parameters))
    ca.parameters --= mapping.urlParamNames.values
    ua.params(ca.parameters)
    if (mapping.httpMethod != null) {
      val method = RequestMapper.HttpMethodMap(mapping.httpMethod)
      if ("" != method) ua.param(RequestMapper.MethodParam, method)
    }
    ua
  }
}

trait Action {
  var suffix: String = _
  val parameters = new collection.mutable.HashMap[String, String]
  def uri: String

  def suffix(suffix: String): this.type = {
    this.suffix = suffix
    this
  }

  def param(key: String, value: String): this.type = {
    parameters.put(key, value)
    this
  }

  def param(key: String, obj: Object): this.type = {
    parameters.put(key, String.valueOf(obj))
    this
  }

  def params(newParams: collection.Map[String, String]): this.type = {
    parameters ++= newParams
    this
  }

  def params(paramStr: String): this.type = {
    if (Strings.isNotEmpty(paramStr)) {
      val paramPairs = Strings.split(paramStr, "&")
      for (paramPair <- paramPairs) {
        val key = Strings.substringBefore(paramPair, "=")
        val value = Strings.substringAfter(paramPair, "=")
        if (Strings.isNotEmpty(key) && Strings.isNotEmpty(value)) {
          parameters.put(key, value)
        }
      }
    }
    this
  }

  def url: String = {
    val buf = new StringBuilder(uri)
    if (null != suffix) buf.append(suffix)
    if (null != parameters && parameters.size > 0) {
      var first = true
      for ((key, value) <- parameters) {
        try {
          if (first) {
            buf.append('?')
            first = false
          } else {
            buf.append('&')
          }
          buf.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"))
        } catch {
          case e: Exception => throw new RuntimeException(e.getMessage())
        }
      }
    }
    return buf.toString()
  }
}

class ClassAction(val clazz: Class[_], val method: String) extends Action {
  var uri: String = _
}

class StrutsAction(val namespace: String, val name: String, val method: String, val path: String = null) extends Action {
  val uri = if (null == path) buildUri() else path

  def buildUri(): String = {
    val buf = new StringBuilder(40)
    if (null == namespace || namespace.length() == 1) buf.append('/')
    else buf.append(namespace).append('/')

    if (null != name) buf.append(name)
    if (Strings.isNotEmpty(method)) buf.append('/').append(method)
    buf.toString
  }
}

class URIAction(val uri: String) extends Action {

  def toStruts: StrutsAction = {
    var endIndex = uri.length
    var actionIndex = 0
    var bandIndex = -1 //!
    var nonSlash = true
    var i = endIndex - 1
    while (i > -1 && nonSlash) {
      uri.charAt(i) match {
        case '.' => endIndex = i
        case '!' =>
          endIndex = i; bandIndex = i
        case '/' =>
          actionIndex = i + 1; nonSlash = false
        case _ =>
      }
      i -= 1
    }
    val namespace = if (actionIndex < 2) "/" else uri.substring(0, actionIndex - 1)
    val actionName = uri.substring(actionIndex, endIndex)
    val methodName = if (bandIndex > 0) uri.substring(bandIndex, endIndex) else null
    val sa = new StrutsAction(namespace, actionName, methodName)
    sa.params(parameters)
    sa.suffix = suffix
    sa
  }
}

class RequestMapping(val action: ActionMapping, val handler: Handler, val params: collection.Map[String, Any])

import org.beangle.commons.http.HttpMethods._
class ActionMapping(val httpMethod: String, val url: String, val clazz: Class[_], val method: String, val paramNames: Array[String], val urlParamNames: Map[Integer, String], val namespace: String, val name: String) {
  val isPattern = url.contains("{") || url.contains("*")

  def httpMethodMatches(requestMethod: String): Boolean = {
    if (null == httpMethod) requestMethod == GET || requestMethod == POST
    else requestMethod == httpMethod
  }
  def fill(params: collection.Map[String, Any]): String = {
    if (!isPattern) return url
    val result = url
    val parts = Strings.split(url, '/')
    urlParamNames foreach {
      case (index, name) =>
        parts(index) = String.valueOf(params(name))
    }
    "/" + Strings.join(parts, "/")
  }

  override def toString: String = {
    (if (null == httpMethod) "*" else httpMethod) + " " + url + " " + clazz.getName + "." +
      method + "(" + Strings.join(paramNames, ",") + ") " + namespace + "(" + name + ")"
  }
}

trait ActionBuilder {

  def build(clazz: Class[_], method: String): Action
}

trait ActionMappingBuilder {

  def build(clazz: Class[_]): Seq[Tuple2[ActionMapping, Method]]
}