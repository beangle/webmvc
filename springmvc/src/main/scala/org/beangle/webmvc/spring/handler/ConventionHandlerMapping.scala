package org.beangle.webmvc.spring.handler

import org.beangle.commons.text.i18n.TextResourceProvider
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.dispatch.RequestMapper
import org.springframework.web.servlet.{ HandlerExecutionChain, HandlerMapping }

import javax.servlet.http.HttpServletRequest

class ConventionHandlerMapping(configurer: Configurer) extends HandlerMapping {

  var mapper: RequestMapper = _

  var localeResover: org.beangle.webmvc.context.LocaleResolver = _

  var textResourceProvider: TextResourceProvider = _

  override def getHandler(request: HttpServletRequest): HandlerExecutionChain = {
    mapper.resolve(request) match {
      case Some(am) =>
        ActionContextHelper.build(request, null, am, localeResover, textResourceProvider)
        new HandlerExecutionChain(am.handler)
      case None =>
        null
    }
  }
}