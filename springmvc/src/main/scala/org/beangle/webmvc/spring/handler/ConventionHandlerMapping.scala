package org.beangle.webmvc.spring.handler

import java.{util => ju}

import org.beangle.webmvc.route.{ActionFinder, RouteService}
import org.beangle.webmvc.route.impl.DefaultURIResolver
import org.beangle.webmvc.spring.handler.Constants.ActionMappingName
import org.springframework.web.servlet.HandlerExecutionChain
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping

import javax.servlet.http.HttpServletRequest

class ConventionHandlerMapping(routeService: RouteService) extends AbstractDetectingUrlHandlerMapping {

  val resolver = new DefaultURIResolver

  private val actionPackages = {
    val packages = new collection.mutable.ListBuffer[String]
    routeService.profiles foreach { profile =>
      if (profile.actionScan) packages += profile.actionPattern
    }
    packages.toList
  }

  var actionSuffix: String = "Action"

  private var test = new ActionFinder.Test(actionSuffix, actionPackages)

  /**
   * generate url for handler
   */
  protected override def determineUrlsForHandler(beanName: String): Array[String] = {
    if (test(beanName)) {
      val action = routeService.buildAction(beanName)
      action.method = null
      Array(action.getUri)
    } else null
  }

  protected override def getHandlerInternal(request: HttpServletRequest): Object = {
    val am = resolver.resolve(request)
    request.setAttribute(ActionMappingName, am)
    lookupHandler(am.namespace + "/" + am.name, request)
  }

  protected override def buildPathExposingHandler(rawHandler: Object, bestMatchingPattern: String,
    pathWithinMapping: String, uriTemplateVariables: ju.Map[String, String]): Object = {
    val chain = new HandlerExecutionChain(rawHandler)
    chain.addInterceptor(new BeangleInterceptor());
    chain
  }
}