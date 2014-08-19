package org.beangle.webmvc.dispatch.impl

import java.lang.reflect.Method
import org.beangle.webmvc.dispatch.ActionMapping
import org.beangle.webmvc.execution.impl.MethodHandler
import org.beangle.webmvc.dispatch.RequestMapping

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