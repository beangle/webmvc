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

  def build(action: ActionMapping, bean: AnyRef, method: Method): RequestMapping = {
    new RequestMapping(action, new MethodHandler(bean, method), Map.empty)
  }
}