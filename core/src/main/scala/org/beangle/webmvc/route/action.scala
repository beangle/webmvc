package org.beangle.webmvc.route

import java.net.URLEncoder
import org.beangle.commons.lang.{ Objects, Strings }
import java.lang.reflect.Method

object Action {
  def to(clazz: Class[_]): Action = new Action(clazz, null)

  def to(obj: Object): Action = new Action(obj, null)

  def parse(path: String): Array[String] = {
    var endIndex = path.length()
    var i = endIndex - 1
    var actionIndex = 0
    var flag = false
    while (i > -1 && !flag) {
      var c = path.charAt(i)
      if (c == '.' || c == '!') {
        endIndex = i
      } else if (c == '/') {
        actionIndex = i + 1
        flag = true
      }
      i -= 1
    }
    var namespace: String = null
    if (actionIndex < 2) {
      namespace = "/"
    } else {
      namespace = path.substring(0, actionIndex - 1)
      if (namespace.charAt(0) != '/') {
        namespace = "/" + namespace
      }
    }
    val actionName = path.substring(actionIndex, endIndex)
    Array(namespace, actionName)
  }
}

class Action(val clazz: Class[_], var namespace: String, var name: String, var method: String) {

  var path: String = _

  var suffix: String = _

  val parameters = new collection.mutable.HashMap[String, String]

  def this(method: String) {
    this(null, null, null, method)
  }

  def this(ctlObj: Object, method: String) {
    this(if (ctlObj != null) ctlObj.getClass() else null, null, null, method)
  }

  def this(clazz: Class[_], method: String) {
    this(clazz, null, null, method)
  }

  def this(clazz: Class[_], method: String, params: String) {
    this(clazz, null, null, method)
    this.params(params)
  }

  //FIXME use factory
  def this(url: String, method: String) {
    this(null, if (url == null) null else Action.parse(url)(0), if (url == null) null else Action.parse(url)(1), method)
  }

  def this(url: String, method: String, params: String) {
    this(null, if (url == null) null else Action.parse(url)(0), if (url == null) null else Action.parse(url)(1), method)
    this.params(params)
  }

  def name(name: String): Action = {
    this.name = name
    this
  }

  def namespace(namespace: String): Action = {
    this.namespace = namespace
    this
  }

  def method(method: String): Action = {
    this.method = method
    this
  }

  def suffix(suffix: String): Action = {
    this.suffix = suffix
    this
  }

  def param(key: String, value: String): Action = {
    parameters.put(key, value)
    this
  }

  def param(key: String, obj: Object): Action = {
    parameters.put(key, String.valueOf(obj))
    this
  }

  def params(newParams: collection.Map[String, String]): Action = {
    parameters ++= newParams
    this
  }

  def params(paramStr: String): Action = {
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

  def path(path: String): Action = {
    val data = Action.parse(path)
    namespace = data(0)
    name = data(1)
    this
  }

  def getUri(methodSeparator: Char = '!'): String = {
    val buf = new StringBuilder(25)
    if (null == namespace || namespace.length() == 1) buf.append('/')
    else buf.append(namespace).append('/')

    if (null != name) buf.append(name)
    if (Strings.isNotEmpty(method)) buf.append(methodSeparator).append(method)
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

  override def toString(): String =
    Objects.toStringBuilder(this).add("namespace", namespace).add("name", name).add("method", method)
      .add("params", parameters).toString()
}

case class RequestMapping(action: ActionMapping, handler: Handler, params: Map[String, Any])

class ActionMapping(val httpMethod: String, val url: String, val clazz: Class[_], val method: String, val paramNames: Array[String], val urlParamNames: Map[Integer, String], val namespace: String, val name: String) {
  val isPattern = url.contains("{") || url.contains("*")

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