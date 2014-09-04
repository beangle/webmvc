package org.beangle.webmvc.dispatch.impl

import java.{ lang => jl }

import org.beangle.commons.http.HttpMethods.GET
import org.beangle.commons.inject.{ Container, ContainerRefreshedHook }
import org.beangle.commons.lang.Strings.{ isNotEmpty, split }
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.config.{ ActionConfig, ActionMapping }
import org.beangle.webmvc.config.{ ActionMappingBuilder, Configurer }
import org.beangle.webmvc.config.ActionMapping.{ DefaultMethod, HttpMethodMap, HttpMethods, MethodParam }
import org.beangle.webmvc.context.ActionFinder
import org.beangle.webmvc.dispatch.{ RequestMapper, RequestMapping }

import javax.servlet.http.HttpServletRequest

@description("支持层级的url映射器")
class HierarchicalUrlMapper extends RequestMapper with ContainerRefreshedHook with Logging {

  private val hierarchicalMappings = new HierarchicalMappings

  private val directMappings = new collection.mutable.HashMap[String, RequestMapping]

  //reverse mapping
  private val actionConfigMap = new collection.mutable.HashMap[String, ActionConfig]

  var actionFinder: ActionFinder = _
  var configurer: Configurer = _
  var actionMappingBuilder: ActionMappingBuilder = _

  override def notify(container: Container): Unit = {
    val watch = new Stopwatch(true)
    var actionCount, mappingCount = 0
    actionFinder.getActions(new ActionFinder.Test(configurer)) foreach { bean =>
      val clazz = bean.getClass
      actionCount += 1
      actionMappingBuilder.build(clazz, configurer.getProfile(clazz.getName)).map {
        case (url, action) =>
          mappingCount += 1
          add(url, RequestMappingBuilder.build(action, bean))
      }
    }
    info(s"Action scan completed,create $actionCount actions($mappingCount mappings) in ${watch}.")
  }

  def actionConfigs = actionConfigMap.values.toSet

  private def add(url: String, mapping: RequestMapping): Unit = {
    val action = mapping.action
    actionConfigMap.put(action.config.clazz.getName, action.config)
    actionConfigMap.put(action.config.name, action.config)

    val finalUrl = if (null != action.httpMethod && isNotEmpty(HttpMethodMap(action.httpMethod))) url + "/" + HttpMethodMap(action.httpMethod) else url
    if (!finalUrl.contains("{")) directMappings.put(finalUrl, mapping)
    else hierarchicalMappings.add(finalUrl, mapping)
  }

  override def antiResolve(name: String, method: String): Option[ActionMapping] = {
    actionConfigMap.get(name) match {
      case Some(config) => config.mappings.get(method)
      case None => None
    }
  }

  override def antiResolve(name: String): Option[ActionConfig] = {
    actionConfigMap.get(name)
  }

  override def resolve(uri: String): Option[RequestMapping] = {
    val directMapping = directMappings.get(uri)
    if (None != directMapping) return directMapping
    else hierarchicalMappings.resolve(GET, uri)
  }

  override def resolve(request: HttpServletRequest): Option[RequestMapping] = {
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

    val finalUrl = sb.toString
    val directMapping = directMappings.get(finalUrl)
    if (None != directMapping) return directMapping
    else hierarchicalMappings.resolve(request.getMethod, finalUrl)
  }

  private def determineMethod(request: HttpServletRequest, defaultMethod: String): String = {
    var httpMethod = HttpMethodMap(request.getMethod)
    if ("" == httpMethod) {
      httpMethod = request.getParameter(MethodParam)
      if (null != httpMethod && !HttpMethods.contains(httpMethod)) httpMethod = null
    }
    if (null == httpMethod) defaultMethod else httpMethod
  }
}

class HierarchicalMappings {
  val children = new collection.mutable.HashMap[String, HierarchicalMappings]
  val mappings = new collection.mutable.HashMap[String, RequestMapping]

  def add(url: String, mapping: RequestMapping): Unit = {
    add(url, mapping, this)
  }

  def resolve(httpMethod: String, uri: String): Option[RequestMapping] = {
    val parts = split(uri, '/')
    val result = find(0, parts, this)
    result match {
      case Some(m) =>
        val action = m.action
        if (action.httpMethodMatches(httpMethod)) {
          val params = new collection.mutable.HashMap[String, String]
          action.urlParams foreach {
            case (k, v) =>
              params.put(v, parts(k))
          }
          Some(new RequestMapping(action, m.handler, params))
        } else None
      case None => None
    }
  }

  private def add(pattern: String, mapping: RequestMapping, mappings: HierarchicalMappings): Unit = {
    val slashIndex = pattern.indexOf('/', 1)
    val head = if (-1 == slashIndex) pattern.substring(1) else pattern.substring(1, slashIndex)
    val headPattern = RequestMappingBuilder.getMatcherName(head)

    if (-1 == slashIndex) {
      mappings.mappings.put(headPattern, mapping)
    } else {
      add(pattern.substring(slashIndex), mapping, mappings.children.getOrElseUpdate(headPattern, new HierarchicalMappings))
    }
  }

  private def find(index: Int, parts: Array[String], mappings: HierarchicalMappings): Option[RequestMapping] = {
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
