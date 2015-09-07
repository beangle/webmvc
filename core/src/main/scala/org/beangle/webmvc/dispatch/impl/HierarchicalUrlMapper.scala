/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.webmvc.dispatch.impl

import java.{ lang => jl }

import org.beangle.commons.http.HttpMethods.{ GET, POST }
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.Strings.split
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.config.ActionMapping.{ DefaultMethod, MethodParam }
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.dispatch.{ RequestMapper, RequestMapping }
import org.beangle.webmvc.execution.HandlerBuilder

import javax.servlet.http.HttpServletRequest

@description("支持层级的url映射器")
class HierarchicalUrlMapper extends RequestMapper with Logging {

  private val hierarchicalMappings = new HierarchicalMappings

  private val directMappings = new collection.mutable.HashMap[String, MethodMappings]

  var configurer: Configurer = _

  var handlerBuilder: HandlerBuilder = _

  override def start(container: Container): Unit = {
    configurer.build() foreach {
      case (url, actionmapping, bean) =>
        add(url, new RequestMapping(actionmapping, handlerBuilder.build(bean, actionmapping), Map.empty))
    }
  }

  private def add(url: String, mapping: RequestMapping): Unit = {
    val action = mapping.action
    if (!url.contains("{")) {
      directMappings.getOrElseUpdate(url, new MethodMappings).methods.put(action.httpMethod, mapping)
    } else hierarchicalMappings.add(action.httpMethod, url, mapping)
  }

  override def resolve(uri: String): Option[RequestMapping] = {
    val directMapping = directMappings.get(uri) match {
      case Some(m) => m.get(POST)
      case None => None
    }
    if (None != directMapping) return directMapping
    else hierarchicalMappings.resolve(GET, uri)
  }

  override def resolve(request: HttpServletRequest): Option[RequestMapping] = {
    val uri = RequestUtils.getServletPath(request)
    var bangIdx, dotIdx = -1
    val lastSlashIdx = uri.lastIndexOf('/')
    val sb = new jl.StringBuilder(uri.length + 10)
    if (lastSlashIdx == uri.length - 1) {
      sb.append(uri).append(DefaultMethod)
    } else {
      var i = lastSlashIdx + 2
      var chars = new Array[Char](uri.length)
      uri.getChars(0, chars.length, chars, 0)
      while (i < chars.length && dotIdx == -1) {
        var c = chars(i)
        if ('!' == c) bangIdx = i
        else if ('.' == c) dotIdx = i
        i += 1
      }
      sb.append(chars)
      if (dotIdx > 0) sb.delete(dotIdx, sb.length)
      if (bangIdx > 0) sb.setCharAt(bangIdx, '/')
    }

    val httpMethod = determineHttpMethod(request)
    val finalUrl = sb.toString
    val directMapping = directMappings.get(finalUrl) match {
      case Some(dm) => dm.get(httpMethod)
      case None => None
    }
    if (None != directMapping) return directMapping
    else hierarchicalMappings.resolve(httpMethod, finalUrl)
  }

  private def determineHttpMethod(request: HttpServletRequest): String = {
    var httpMethod = request.getParameter(MethodParam)
    if (null == httpMethod) httpMethod = request.getMethod
    else httpMethod = httpMethod.toUpperCase()
    httpMethod
  }
}

class MethodMappings {
  val methods = new collection.mutable.HashMap[String, RequestMapping]
  def get(method: String): Option[RequestMapping] = {
    var result = methods.get(method)
    if (result.isEmpty && method == POST) result = methods.get(GET)
    result
  }
}

class HierarchicalMappings {
  val children = new collection.mutable.HashMap[String, HierarchicalMappings]
  val mappings = new collection.mutable.HashMap[String, MethodMappings]

  def resolve(httpMethod: String, uri: String): Option[RequestMapping] = {
    val parts = split(uri, '/')
    val result = find(0, parts, this)
    result match {
      case Some(methodMappings) =>
        methodMappings.get(httpMethod) match {
          case Some(m) =>
            val action = m.action
            val params = new collection.mutable.HashMap[String, String]
            action.urlParams foreach {
              case (k, v) =>
                params.put(v, parts(k))
            }
            Some(new RequestMapping(action, m.handler, params))
          case None => None
        }
      case None => None
    }
  }

  def add(httpMethod: String, url: String, mapping: RequestMapping): Unit = {
    add(httpMethod, url, mapping, this)
  }

  private def add(httpMethod: String, pattern: String, mapping: RequestMapping, mappings: HierarchicalMappings): Unit = {
    val slashIndex = pattern.indexOf('/', 1)
    val head = if (-1 == slashIndex) pattern.substring(1) else pattern.substring(1, slashIndex)
    val headPattern = if (head.charAt(0) == '{' && head.charAt(head.length - 1) == '}') "*" else head

    if (-1 == slashIndex) {
      val methodMappings = mappings.mappings.getOrElseUpdate(headPattern, new MethodMappings)
      methodMappings.methods.put(httpMethod, mapping)
    } else {
      add(httpMethod, pattern.substring(slashIndex), mapping, mappings.children.getOrElseUpdate(headPattern, new HierarchicalMappings))
    }
  }

  private def find(index: Int, parts: Array[String], mappings: HierarchicalMappings): Option[MethodMappings] = {
    if (index < parts.length && null != mappings) {
      if (index == parts.length - 1) {
        val mapping = mappings.mappings.get(parts(index))
        if (mapping == None) mappings.mappings.get("*") else mapping
      } else {
        val mapping = find(index + 1, parts, mappings.children.get(parts(index)).orNull)
        if (mapping == None) find(index + 1, parts, mappings.children.get("*").orNull)
        else mapping
      }
    } else {
      None
    }
  }

}
