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

class ConventionHandlerMapping(configurer: Configurer) extends AbstractDetectingUrlHandlerMapping {

  var resolver: HierarchicalUrlMapper = _

  var localeResover: org.beangle.webmvc.context.LocaleResolver = _

  var textResourceProvider: TextResourceProvider = _

  var actionMappingBuilder: ActionMappingBuilder = _
  /**
   * generate url for every handler
   */
  protected override def determineUrlsForHandler(beanName: String): Array[String] = {
    val bean = getApplicationContext().getBean(beanName)
    val actionTest = new ActionFinder.Test(configurer)
    if (actionTest(bean.getClass)) {
      val classInfo = ClassInfo.get(bean.getClass)
      actionMappingBuilder.build(bean.getClass, configurer.getProfile(bean.getClass.getName)).map {
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
      case None =>
        null
      //super.getHandlerInternal(request)
    }
  }

  protected override def getHandlerExecutionChain(handler: Object, request: HttpServletRequest): HandlerExecutionChain = {
    new HandlerExecutionChain(handler)
  }
}