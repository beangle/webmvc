/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.dispatch.impl

import java.{lang => jl}

import jakarta.servlet.http.HttpServletRequest
import org.beangle.cdi.Container
import org.beangle.commons.lang.Strings.split
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.{Arrays, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.http.HttpMethods.{GET, POST}
import org.beangle.webmvc.config.Path
import org.beangle.webmvc.config.RouteMapping.{DefaultMethod, MethodParam}
import org.beangle.webmvc.dispatch.{HandlerHolder, RequestMapper, RouteProvider}
import org.beangle.webmvc.execution.Handler

@description("支持层级的url映射器")
class HierarchicalUrlMapper(container: Container) extends RequestMapper with Logging {

  private val hierarchicalMappings = new HierarchicalMappings

  private val directMappings = new collection.mutable.HashMap[String, HttpMethodMappings]

  override def build(): Unit = {
    container.getBeans(classOf[RouteProvider]) foreach {
      case (_, p) =>
        p.routes foreach { r =>
          add(r.httpMethod, r.url, r.handler)
        }
    }
  }

  def add(httpMethod: String, url: String, handler: Handler): Unit = {
    if (!url.contains("{")) {
      directMappings.getOrElseUpdate(url, new HttpMethodMappings).methods.put(httpMethod, new HandlerHolder(handler, Map.empty))
    } else {
      hierarchicalMappings.add(httpMethod, url, new HandlerHolder(handler, Path.parse(url)))
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

  override def resolve(uri: String, request: HttpServletRequest): Option[HandlerHolder] = {
    var bangIdx, dotIdx = -1
    val lastSlashIdx = uri.lastIndexOf('/')
    val sb = new jl.StringBuilder(uri.length + 10)
    if (lastSlashIdx > 0 && lastSlashIdx == uri.length - 1) {
      sb.append(uri).append(DefaultMethod)
    } else {
      var i = lastSlashIdx + 2
      val chars = new Array[Char](uri.length)
      uri.getChars(0, chars.length, chars, 0)
      while (i < chars.length) {
        val c = chars(i)
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
    if (directMapping.isDefined) directMapping
    else hierarchicalMappings.resolve(httpMethod, finalUrl)
  }

  private def determineHttpMethod(request: HttpServletRequest): String = {
    val httpMethod = request.getParameter(MethodParam)
    if (null == httpMethod) request.getMethod else httpMethod.toUpperCase()
  }
}

/**
 * HttpMethod Mappings
 */
class HttpMethodMappings {
  val methods = new collection.mutable.HashMap[String, HandlerHolder]
  var depth: Int = 0

  def matchesDepth(test: Int): Boolean = {
    this.depth == test || this.depth == -1
  }

  def get(method: String): Option[HandlerHolder] = {
    var result = methods.get(method)
    if (result.isEmpty && method == POST) result = methods.get(GET)
    result
  }
}

class HierarchicalMappings {
  val children = new collection.mutable.HashMap[String, HierarchicalMappings]
  val mappings = new collection.mutable.HashMap[String, HttpMethodMappings]
  var tailRecursion = false

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
    add(httpMethod, 1, url, holder, this)
  }

  private def add(httpMethod: String, depth: Int, pattern: String, holder: HandlerHolder, mappings: HierarchicalMappings): Unit = {
    val slashIndex = pattern.indexOf('/', 1)
    val head = if (-1 == slashIndex) pattern.substring(1) else pattern.substring(1, slashIndex)
    val headPattern = if (Path.isPattern(head)) "*" else head
    val mydepth = if (depth > 0 && Path.isTailPattern(head)) -1 else depth

    if (-1 == slashIndex) {
      val methodMappings = mappings.mappings.getOrElseUpdate(headPattern, new HttpMethodMappings)
      methodMappings.methods.put(httpMethod, holder)
      if (Path.isTailPattern(head)) {
        assert(mappings.children.isEmpty || mappings.children("*") == mappings)
        mappings.children.put("*", mappings)
      }
      methodMappings.depth = mydepth
    } else {
      val nextdepth = if (mydepth > 0) mydepth + 1 else mydepth
      add(httpMethod, nextdepth, pattern.substring(slashIndex), holder, mappings.children.getOrElseUpdate(headPattern, new HierarchicalMappings))
    }
    mappings.tailRecursion = mappings.children.size == 1 && mappings.mappings.size == 1 && mappings.children.get("*").contains(mappings)
  }

  private def filterDepth(hm: Option[HttpMethodMappings], depth: Int): Option[HttpMethodMappings] = {
    hm match {
      case mm@Some(m) => if (m.matchesDepth(depth)) mm else None
      case None => None
    }
  }

  private def find(index: Int, parts: Array[String], mappings: HierarchicalMappings): Option[HttpMethodMappings] = {
    if (index < parts.length && null != mappings) {
      if (mappings.tailRecursion) {
        filterDepth(mappings.mappings.get("*"), parts.length)
      } else if (index == parts.length - 1) {
        val depth = parts.length
        val mapping = filterDepth(mappings.mappings.get(parts(index)), depth)
        if (mapping.isEmpty) filterDepth(mappings.mappings.get("*"), depth) else mapping
      } else {
        val mapping = find(index + 1, parts, mappings.children.get(parts(index)).orNull)
        if (mapping.isEmpty) find(index + 1, parts, mappings.children.get("*").orNull)
        else mapping
      }
    } else {
      None
    }
  }

}
