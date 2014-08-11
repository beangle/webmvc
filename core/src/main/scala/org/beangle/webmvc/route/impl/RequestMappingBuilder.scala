package org.beangle.webmvc.route.impl

import java.lang.reflect.Method

import scala.Range

import org.beangle.commons.lang.Strings
import org.beangle.webmvc.annotation.param
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.route.{ ActionMapping, RequestMapping }

object RequestMappingBuilder {
  /**
   * /{project} etc.
   */
  @inline
  def getMatcherName(name: String): String = {
    if (name.charAt(0) == '{' && name.charAt(name.length - 1) == '}') "*" else name
  }

  def parse(pattern: String): Map[Integer, String] = {
    var parts = Strings.split(pattern, "/")
    var params = new collection.mutable.HashMap[Integer, String]
    var i = 0
    while (i < parts.length) {
      val p = parts(i)
      if (p.charAt(0) == '{' && p.charAt(p.length - 1) == '}') {
        params.put(Integer.valueOf(i), p.substring(1, p.length - 1))
      } else if (p == "*") {
        params.put(Integer.valueOf(i), String.valueOf(i))
      }
      i += 1
    }
    params.toMap
  }

  def build(action: ActionMapping, bean: AnyRef, method: Method): RequestMapping = {
    val urlParams = parse(action.url)
    val urlPathNames = urlParams.keySet.toList.sorted.map { i => urlParams(i) }

    val annotationsList = method.getParameterAnnotations
    val paramNames = Range(0, annotationsList.length) map { i =>
      annotationsList(i).find { ann => ann.isInstanceOf[param] } match {
        case Some(p) => p.asInstanceOf[param].value
        case None => urlPathNames(i)
      }
    }

    if (method.getParameterTypes().length != paramNames.size) throw new RuntimeException("Cannot find enough param name,Using @mapping or @param")

    RequestMapping(action, new MethodHandler(bean, method, paramNames.toArray), Map((ActionContext.URLParams, urlParams)))
  }
}