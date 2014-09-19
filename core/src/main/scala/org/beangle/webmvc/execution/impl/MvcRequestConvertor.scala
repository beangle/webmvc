package org.beangle.webmvc.execution.impl

import org.beangle.commons.security.{ DefaultRequest, Request }
import org.beangle.commons.web.security.RequestConvertor
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.context.ActionContextHelper

import javax.servlet.http.HttpServletRequest

class MvcRequestConvertor extends RequestConvertor {
  def convert(request: HttpServletRequest): Request = {
    val mapping = ActionContextHelper.getMapping(ContextHolder.context)
    new DefaultRequest(mapping.action.config.name, mapping.action.method.getName)
  }
}