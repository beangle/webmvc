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
import org.beangle.commons.lang.{ Arrays, Strings }
import org.beangle.commons.lang.Strings.split
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.config.Path
import org.beangle.webmvc.config.RouteMapping.{ DefaultMethod, MethodParam }
import org.beangle.webmvc.dispatch.{ RequestMapper, HandlerHolder }
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.execution.InvokerBuilder
import org.beangle.webmvc.view.impl.ViewManager
import org.beangle.webmvc.execution.MappingHandler
import org.beangle.webmvc.dispatch.RouteProvider
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.execution.Handler

@description("支持层级的url映射器")
class HierarchicalUrlMapper(container: Container) extends RequestMapper with Logging {

  private val hierarchicalMappings = new HierarchicalMappings

  private val directMappings = new collection.mutable.HashMap[String, HttpMethodMappings]

  override def build(): Unit = {
    container.getBeans(classOf[RouteProvider]) foreach {
      case (n, p) =>
        p.routes foreach { r =>
          add(r.httpMethod, r.url, r.handler)
        }
    }
  }

  def add(httpMethod: String, url: String, handler: Handler): Unit = {
    if (!url.contains("{")) {
      directMappings.getOrElseUpdate(url, new HttpMethodMappings).methods.put(httpMethod, new HandlerHolder(handler, Map.empty))
    } else {
      val holder = new HandlerHolder(handler, Path.parse(url))
      hierarchicalMappings.add(httpMethod, url, holder)
    }
  }

  override def resolve(uri: String): Option[HandlerHolder] = {
    directMappings.get(uri) match {
      case Some(m) =>
        val hh = m.get(POST)
        if (hh.isEmpty) hierarchicalMappings.resolve(GET, uri) else hh
      case None => None
    }
  }

  override def resolve(request: HttpServletRequest): Option[HandlerHolder] = {
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
      while (i < chars.length) {
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
      case None     => None
    }
    if (None != directMapping) return directMapping
    else hierarchicalMappings.resolve(httpMethod, finalUrl)
  }

  private def determineHttpMethod(request: HttpServletRequest): String = {
    var httpMethod = request.getParameter(MethodParam)
    if (null == httpMethod) request.getMethod else httpMethod.toUpperCase()
  }
}

/**
 * HttpMethod Mappings
 */
class HttpMethodMappings {
  val methods = new collection.mutable.HashMap[String, HandlerHolder]
  def get(method: String): Option[HandlerHolder] = {
    var result = methods.get(method)
    if (result.isEmpty && method == POST) result = methods.get(GET)
    result
  }
}

class HierarchicalMappings {
  val children = new collection.mutable.HashMap[String, HierarchicalMappings]
  val mappings = new collection.mutable.HashMap[String, HttpMethodMappings]

  def resolve(httpMethod: String, uri: String): Option[HandlerHolder] = {
    val parts = split(uri, '/')
    find(0, parts, this) match {
      case Some(methodMappings) =>
        methodMappings.get(httpMethod) match {
          case Some(m) =>
            val params = new collection.mutable.HashMap[String, String]
            m.params foreach {
              case (v, i) =>
                val k = i.asInstanceOf[Int]
                if (Path.isTailMatch(v)) params.put(v.substring(0, v.length - 1), Strings.join(Arrays.subarray(parts, k, parts.length), "/"))
                else params.put(v, parts(k))
            }
            Some(new HandlerHolder(m.handler, params))
          case None => None
        }
      case None => None
    }
  }

  def add(httpMethod: String, url: String, holder: HandlerHolder): Unit = {
    add(httpMethod, url, holder, this)
  }

  private def add(httpMethod: String, pattern: String, holder: HandlerHolder, mappings: HierarchicalMappings): Unit = {
    val slashIndex = pattern.indexOf('/', 1)
    val head = if (-1 == slashIndex) pattern.substring(1) else pattern.substring(1, slashIndex)
    val headPattern = if (Path.isPattern(head)) "*" else head

    if (-1 == slashIndex) {
      val methodMappings = mappings.mappings.getOrElseUpdate(headPattern, new HttpMethodMappings)
      methodMappings.methods.put(httpMethod, holder)
      if (Path.isTailPattern(head)) {
        assert(mappings.children.isEmpty || mappings.children("*") == mappings)
        mappings.children.put("*", mappings)
      }
    } else {
      add(httpMethod, pattern.substring(slashIndex), holder, mappings.children.getOrElseUpdate(headPattern, new HierarchicalMappings))
    }
  }

  private def find(index: Int, parts: Array[String], mappings: HierarchicalMappings): Option[HttpMethodMappings] = {
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
