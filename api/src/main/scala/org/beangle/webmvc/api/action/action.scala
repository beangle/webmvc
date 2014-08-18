package org.beangle.webmvc.api.action

import java.net.URLEncoder

import org.beangle.commons.lang.Strings

object to {
  def apply(clazz: Class[_], method: String = "index"): ToClass = {
    new ToClass(clazz, method)
  }

  def apply(obj: Object, method: String): ToClass = {
    new ToClass(obj.getClass, method)
  }

  def apply(clazz: Class[_], method: String, params: String): ToClass = {
    new ToClass(clazz, method).params(params)
  }

  def apply(obj: Object, method: String, params: String): ToClass = {
    new ToClass(obj.getClass, method).params(params)
  }

  def apply(uri: String, params: String): ToURI = {
    new ToURI(uri).params(params)
  }

  def apply(uri: String): ToURI = {
    new ToURI(uri)
  }
}

trait To {
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

class ToClass(val clazz: Class[_], val method: String) extends To {
  var uri: String = _
}

class ToStruts(val namespace: String, val name: String, val method: String, val path: String = null) extends To {
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

class ToURI(val uri: String) extends To {

  def toStruts: ToStruts = {
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
    val sa = new ToStruts(namespace, actionName, methodName)
    sa.params(parameters)
    sa.suffix = suffix
    sa
  }
}
