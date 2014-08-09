package org.beangle.webmvc.spring.handler

import java.{ util => ju }
import org.beangle.webmvc.route.{ ActionFinder, RouteService }
import org.beangle.webmvc.route.impl.{ ActionMappings, HierarchicalUrlMapper }
import org.springframework.web.servlet.HandlerExecutionChain
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping
import javax.servlet.http.HttpServletRequest
import org.springframework.beans.BeansException
import org.beangle.webmvc.route.RequestMapper
import org.beangle.webmvc.route.ActionMapping
import org.beangle.webmvc.route.impl.MethodHandler
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.context.ActionContextBuilder

class ConventionHandlerMapping(routeService: RouteService) extends AbstractDetectingUrlHandlerMapping {

  var actionSuffix: String = "Action"

  var resolver: HierarchicalUrlMapper = _

  private val actionTest = {
    val packages = new collection.mutable.ListBuffer[String]
    routeService.profiles foreach { profile =>
      if (profile.actionScan) packages += profile.actionPattern
    }
    new ActionFinder.Test(actionSuffix, packages.toList)
  }

  /**
   * generate url for every handler
   */
  protected override def determineUrlsForHandler(beanName: String): Array[String] = {
    val bean = getApplicationContext().getBean(beanName)
    if (actionTest(bean.getClass.getName)) {
      val patterns = new collection.mutable.ListBuffer[String]
      val classInfo = ClassInfo.get(bean.getClass)
      routeService.buildActions(bean.getClass).map { action =>
        val methods = classInfo.getMethods(action.method)
        if (methods.size == 1) {
          val pattern = action.getUri()
          resolver.add(ActionMapping(pattern, MethodHandler(bean, methods.head.method), null))
          patterns += pattern
        }
      }
      patterns.toArray
    } else null
  }

  protected override def getHandlerInternal(request: HttpServletRequest): Object = {

    resolver.resolve(request) match {
      case Some(am) =>
        val rs = lookupHandler(am.url, request)
        if (null != rs) {
          val context = ActionContextBuilder.build(request, null)
          ContextHolder.contexts.set(context)
        } else {
          ContextHolder.contexts.remove()
        }
        rs
      case None =>
        ContextHolder.contexts.remove()
        null
    }

  }

  protected override def buildPathExposingHandler(rawHandler: Object, bestMatchingPattern: String,
    pathWithinMapping: String, uriTemplateVariables: ju.Map[String, String]): Object = {
    val chain = new HandlerExecutionChain(rawHandler)
    chain.addInterceptor(new BeangleInterceptor());
    chain
  }
}