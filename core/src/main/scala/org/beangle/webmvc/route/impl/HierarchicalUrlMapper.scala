package org.beangle.webmvc.route.impl

import java.{lang => jl}

import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.route.{ActionMapping, RequestMapper}

import javax.servlet.http.HttpServletRequest

class HierarchicalUrlMapper extends RequestMapper {
  private val mappings = new ActionMappings

  val DefaultMethod = "index"
  val MethodParam = "_method"

  def add(mapping: ActionMapping): Unit = {
    mappings.add(mapping)
  }

  def resolve(uri: String): Option[ActionMapping] = {
    mappings.resolve(uri)
  }

  def resolve(request: HttpServletRequest): Option[ActionMapping] = {
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
    mappings.resolve(sb.toString)
  }

  private def determineMethod(request: HttpServletRequest, defaultMethod: String): String = {
    val method = request.getParameter(MethodParam)
    if (null == method) defaultMethod else method
  }
}

class ActionMappings {
  val children = new collection.mutable.HashMap[String, ActionMappings]
  val mappings = new collection.mutable.HashMap[String, ActionMapping]

  def add(mapping: ActionMapping): Unit = {
    if (mapping.isPattern) add(mapping.url, mapping, this)
    else mappings.put(mapping.url, mapping)
  }

  def add(pattern: String, mapping: ActionMapping, mappings: ActionMappings): Unit = {
    val slashIndex = pattern.indexOf('/', 1)
    val head = if (-1 == slashIndex) pattern.substring(1) else pattern.substring(1, slashIndex)
    val headPattern = ActionMapping.matcherName(head)

    if (-1 == slashIndex) {
      mappings.mappings.put(headPattern, mapping)
    } else {
      add(pattern.substring(slashIndex), mapping, mappings.children.getOrElseUpdate(headPattern, new ActionMappings))
    }
  }

  def resolve(uri: String): Option[ActionMapping] = {
    val directMapping = mappings.get(uri)
    if (None != directMapping) return directMapping

    val parts = Strings.split(uri, '/')
    find(0, parts, this) match {
      case Some(m) =>
        if (m.isPattern) {
          val urlParams = new collection.mutable.HashMap[String, String]
          m.params(ActionContext.URLParams).asInstanceOf[Map[Integer, String]] foreach {
            case (k, v) =>
              urlParams.put(v, parts(k))
          }
          Some(ActionMapping(m.url, m.handler, m.namespace, m.name, m.params ++ urlParams))
        } else Some(m)
      case None => None
    }
  }

  def find(index: Int, parts: Array[String], mappings: ActionMappings): Option[ActionMapping] = {
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
