package org.beangle.webmvc.route.impl

import java.{ lang => jl }
import org.beangle.commons.http.HttpMethods.{ DELETE, GET, HEAD, POST, PUT }
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.route.{ RequestMapper, RequestMapping }
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.route.ActionMapping
import scala.collection.mutable

class HierarchicalUrlMapper extends RequestMapper {
  private val mappings = new RequestMappings
  private val reverseMappings = new collection.mutable.HashMap[Class[_], mutable.Map[String, ActionMapping]]

  val DefaultMethod = "index"
  val MethodParam = "_method"
  val httpMethods = Map((GET, ""), (POST, ""), (PUT, "put"), (DELETE, "delete"), (HEAD, "head"))

  def add(mapping: RequestMapping): Unit = {
    val action = mapping.action
    val methodMappings = reverseMappings.getOrElseUpdate(action.clazz, new mutable.HashMap[String, ActionMapping])
    methodMappings.put(action.method, action)
    mappings.add(mapping)
  }

  def resolve(uri: String): Option[RequestMapping] = {
    mappings.resolve(GET, uri)
  }

  def resolve(request: HttpServletRequest): Option[RequestMapping] = {
    val uri = RequestUtils.getServletPath(request)
    var bangIdx, dotIdx = -1
    val lastSlashIdx = uri.lastIndexOf('/')
    val sb = new jl.StringBuilder(uri.length + 10)
    if (lastSlashIdx == uri.length - 1) {
      sb.append(uri).append(determineMethod(request, DefaultMethod))
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
      else {
        val method = determineMethod(request, null)
        if (null != method && -1 == sb.indexOf(method, lastSlashIdx + 1)) sb.append('/').append(method)
      }
    }
    mappings.resolve(request.getMethod, sb.toString)
  }

  private def determineMethod(request: HttpServletRequest, defaultMethod: String): String = {
    var httpMethod = httpMethods(request.getMethod)
    if ("" == httpMethod) httpMethod = request.getParameter(MethodParam)
    if (null == httpMethod) defaultMethod else httpMethod
  }
}

class RequestMappings {
  val children = new collection.mutable.HashMap[String, RequestMappings]
  val mappings = new collection.mutable.HashMap[String, RequestMapping]

  def add(mapping: RequestMapping): Unit = {
    val action = mapping.action
    val url = if (null != action.httpMethod) action.url + "/" + action.httpMethod.toLowerCase() else action.url

    if (action.isPattern) add(url, mapping, this)
    else mappings.put(action.url, mapping)
  }

  def add(pattern: String, mapping: RequestMapping, mappings: RequestMappings): Unit = {
    val slashIndex = pattern.indexOf('/', 1)
    val head = if (-1 == slashIndex) pattern.substring(1) else pattern.substring(1, slashIndex)
    val headPattern = RequestMappingBuilder.getMatcherName(head)

    if (-1 == slashIndex) {
      mappings.mappings.put(headPattern, mapping)
    } else {
      add(pattern.substring(slashIndex), mapping, mappings.children.getOrElseUpdate(headPattern, new RequestMappings))
    }
  }

  def resolve(httpMethod: String, uri: String): Option[RequestMapping] = {
    val directMapping = mappings.get(uri)
    if (None != directMapping) return directMapping

    val parts = Strings.split(uri, '/')
    val result = find(0, parts, this)
    result match {
      case Some(m) =>
        val action = m.action
        if (null != action.httpMethod && action.httpMethod != httpMethod) None
        else {
          if (action.isPattern) {
            val urlParams = new collection.mutable.HashMap[String, String]
            action.urlParamNames foreach {
              case (k, v) =>
                urlParams.put(v, parts(k))
            }
            Some(RequestMapping(action, m.handler, m.params ++ urlParams))
          } else result
        }
      case None => None
    }
  }

  def find(index: Int, parts: Array[String], mappings: RequestMappings): Option[RequestMapping] = {
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
