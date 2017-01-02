/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.api.action

import java.net.URLEncoder

import org.beangle.commons.lang.Strings

object To {
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

  def apply(uri: String, params: String): ToURL = {
    new ToURL(uri).params(params)
  }

  def apply(uri: String): ToURL = {
    val index = uri.indexOf('?')
    if (-1 == index) new ToURL(uri) else new ToURL(uri.substring(0, index)).params(uri.substring(index + 1))
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

  def param(key: String, obj: Any): this.type = {
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
    if (parameters.size > 0) {
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
    buf.toString
  }
}

class ToClass(val clazz: Class[_], val method: String) extends To {
  var uri: String = _
}

class ToStruts(val namespace: String, val name: String, val method: String, val path: String = null) extends To {
  def uri: String = {
    if (null != path) return path
    val buf = new StringBuilder(40)
    if (null == namespace) buf.append('/')
    else buf.append(namespace).append('/')

    if (null != name) buf.append(name)
    if (null != method) buf.append('/').append(method)
    buf.toString
  }
}

class ToURL(val uri: String) extends To {

  def toStruts: ToStruts = {
    var endIndex = uri.length
    var actionIndex = 0
    var bandIndex = -1 //!
    var nonSlash = true
    var i = endIndex - 1
    while (i > -1 && nonSlash) {
      uri.charAt(i) match {
        case '.' => endIndex = i
        case '!' => bandIndex = i
        case '/' =>
          actionIndex = i + 1; nonSlash = false
        case _ =>
      }
      i -= 1
    }
    val namespace = if (actionIndex < 2) "" else uri.substring(0, actionIndex - 1)
    val actionName = uri.substring(actionIndex, if (bandIndex > 0) bandIndex else endIndex)
    val methodName = if (bandIndex > 0 && bandIndex < endIndex) uri.substring(bandIndex + 1, endIndex) else null
    val sa = new ToStruts(namespace, actionName, methodName)
    if (!parameters.isEmpty) sa.params(parameters)
    sa.suffix = suffix
    sa
  }
}
