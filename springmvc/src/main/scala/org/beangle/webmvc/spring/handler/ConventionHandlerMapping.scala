package org.beangle.webmvc.spring.handler

import java.{ util => ju }
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.webmvc.dispatch.{ HierarchicalUrlMapper, RequestMappingBuilder }
import org.springframework.web.servlet.HandlerExecutionChain
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.text.i18n.spi.TextFormater
import org.beangle.commons.text.i18n.spi.TextBundleRegistry
import org.beangle.commons.bean.Initializing
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.context.ActionFinder
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.commons.text.i18n.spi.TextResourceProvider
import org.beangle.webmvc.context.ActionTextResource

class ConventionHandlerMapping(configurer: Configurer) extends AbstractDetectingUrlHandlerMapping {

  var resolver: HierarchicalUrlMapper = _

  var localeResover: org.beangle.webmvc.spi.context.LocaleResolver = _

  var textResourceProvider: TextResourceProvider = _

  /**
   * generate url for every handler
   */
  protected override def determineUrlsForHandler(beanName: String): Array[String] = {
    val bean = getApplicationContext().getBean(beanName)
    val actionTest = new ActionFinder.Test(configurer)
    if (actionTest(bean.getClass)) {
      val classInfo = ClassInfo.get(bean.getClass)
      configurer.buildMappings(bean.getClass).map {
        case (action, method) =>
          resolver.add(RequestMappingBuilder.build(action, bean, method))
      }
      null
    } else null
  }

  protected override def getHandlerInternal(request: HttpServletRequest): Object = {
    resolver.resolve(request) match {
      case Some(am) =>
        //FIXME for uploads
        val context = ActionContextHelper.build(request, null, localeResover, am)
        ContextHolder.contexts.set(context)
        context.textResource = textResourceProvider.getTextResource(context.locale)
        am.handler
      case None => null
    }
  }

  protected override def getHandlerExecutionChain(handler: Object, request: HttpServletRequest): HandlerExecutionChain = {
    val chain = new HandlerExecutionChain(handler)
    chain.addInterceptor(FlashInterceptor)
    chain
  }
}