package org.beangle.webmvc.spring.handler

import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.commons.text.i18n.TextResourceProvider
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.{ ActionContextHelper, ActionFinder }
import org.beangle.webmvc.dispatch.ActionMappingBuilder
import org.beangle.webmvc.dispatch.impl.{ HierarchicalUrlMapper, RequestMappingBuilder }
import org.springframework.web.servlet.HandlerExecutionChain
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.dispatch.RequestMapper
import org.springframework.web.servlet.HandlerMapping

class ConventionHandlerMapping(configurer: Configurer) extends HandlerMapping {

  var mapper: RequestMapper = _

  var localeResover: org.beangle.webmvc.context.LocaleResolver = _

  var textResourceProvider: TextResourceProvider = _

  override def getHandler(request: HttpServletRequest): HandlerExecutionChain = {
    mapper.resolve(request) match {
      case Some(am) =>
        //FIXME for uploads
        ActionContextHelper.build(request, null, am, localeResover, textResourceProvider)
        new HandlerExecutionChain(am.handler)
      case None =>
        null
    }
  }
}