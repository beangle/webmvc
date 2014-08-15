package org.beangle.webmvc.spring.handler

import java.{ util => ju }
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.webmvc.context.{ ActionContextBuilder, ContextHolder }
import org.beangle.webmvc.route.{ ActionFinder, RouteService }
import org.beangle.webmvc.route.impl.{ HierarchicalUrlMapper, RequestMappingBuilder }
import org.springframework.web.servlet.HandlerExecutionChain
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.action.ActionTextResource
import org.beangle.commons.text.i18n.spi.TextFormater
import org.beangle.commons.text.i18n.spi.TextBundleRegistry
import org.beangle.commons.bean.Initializing

class ConventionHandlerMapping(routeService: RouteService) extends AbstractDetectingUrlHandlerMapping with Initializing {

  var actionSuffix: String = "Action"

  var resolver: HierarchicalUrlMapper = _

  var registry: TextBundleRegistry = _

  var formater: TextFormater = _

  var localeResover: org.beangle.webmvc.context.LocaleResolver = _

  override def init() {
    registry.addDefaults("beangle", "application")
  }

  /**
   * generate url for every handler
   */
  protected override def determineUrlsForHandler(beanName: String): Array[String] = {
    val bean = getApplicationContext().getBean(beanName)
    val actionTest = new ActionFinder.Test(actionSuffix, routeService)
    if (actionTest(bean.getClass)) {
      val patterns = new collection.mutable.ListBuffer[String]
      val classInfo = ClassInfo.get(bean.getClass)
      routeService.buildMappings(bean.getClass).map {
        case (action, method) =>
          resolver.add(RequestMappingBuilder.build(action, bean, method))
          patterns += action.url
      }
      null
    } else null
  }

  protected override def getHandlerInternal(request: HttpServletRequest): Object = {
    resolver.resolve(request) match {
      case Some(am) =>
        //FIXME for uploads
        val context = ActionContextBuilder.build(request, null, localeResover, am)
        context.textResource = new ActionTextResource(am.action.clazz, context.locale, registry, formater)
        ContextHolder.contexts.set(context)
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