package org.beangle.webmvc.webxml

import java.util.EnumSet

import org.beangle.commons.web.session.HttpSessionEventPublisher
import org.beangle.spring.web.ContextListener
import org.beangle.webmvc.dispatch.DispatcherFilter

import javax.servlet.{ DispatcherType, ServletContext }

class Initializer extends org.beangle.commons.web.init.Initializer {

  override def onStartup(sc: ServletContext) {
    sc.setInitParameter("templatePath", "class://")
    sc.setInitParameter("contextConfigLocation", "classpath:spring-context.xml")
    sc.setInitParameter("childContextConfigLocation", "WebApplicationContext:Action@classpath:spring-web-context.xml")

    addListener(new ContextListener)
    sc.addListener(new HttpSessionEventPublisher)
    val action = sc.addFilter("Action", new DispatcherFilter)
    action.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*")
  }
}