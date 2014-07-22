package org.beangle.webmvc.route

import java.net.URLEncoder

import org.beangle.commons.lang.{ Objects, Strings }
import Action._
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

class Action(var namespace: String, var name: String, val clazz: String, var method: String) {

  var path: String = _

  private var extention: String = _

  val parameters = new collection.mutable.HashMap[String, String]

  def this(method: String) {
    this(null, null, null, method)
  }

  def this(ctlObj: Object, method: String) {
    this(null, null, if (ctlObj != null) ctlObj.getClass().getName() else null, method)
  }

  def this(clazz: Class[_], method: String) {
    this(null, null, if (null != clazz) clazz.getName() else null, method)
  }

  def this(clazz: Class[_], method: String, params: String) {
    this(null, null, if (null != clazz) clazz.getName() else null, method)
    this.params(params)
  }

  def this(actionName: String, method: String) {
    this(if (actionName == null) null else parse(actionName)(0), if (actionName == null) null else parse(actionName)(1), null, method)
  }

  def this(actionName: String, method: String, params: String) {
    this(if (actionName == null) null else parse(actionName)(0), if (actionName == null) null else parse(actionName)(1), null, method)
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

  def extention(extention: String): Action = {
    this.extention = extention
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
    val data = parse(path)
    namespace = data(0)
    name = data(1)
    this
  }

  def getUri(): String = {
    val buf = new StringBuilder(25)
    if (null == namespace || namespace.length() == 1) buf.append('/')
    else buf.append(namespace).append('/')
    
    if (null != name) buf.append(name)
    if (Strings.isNotEmpty(method)) buf.append('!').append(method)
    if (null != extention) buf.append('.').append(extention)

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
