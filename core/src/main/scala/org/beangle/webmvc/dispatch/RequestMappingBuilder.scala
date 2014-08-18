package org.beangle.webmvc.dispatch

import java.lang.reflect.Method

import org.beangle.webmvc.config.ActionMapping
import org.beangle.webmvc.spi.dispatch.RequestMapping

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