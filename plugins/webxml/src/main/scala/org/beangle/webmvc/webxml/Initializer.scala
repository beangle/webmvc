package org.beangle.webmvc.webxml

import org.beangle.commons.web.resource.StaticResourceServlet
import org.beangle.commons.web.session.HttpSessionEventPublisher
import org.beangle.spring.web.ContextListener
import org.beangle.webmvc.dispatch.DispatcherServlet

import javax.servlet.ServletContext

class Initializer extends org.beangle.commons.web.init.Initializer {

  override def onStartup(sc: ServletContext) {
    sc.setInitParameter("templatePath", "webapp://pages,class://")
    sc.setInitParameter("contextConfigLocation", "classpath:spring-context.xml")
    sc.setInitParameter("childContextConfigLocation", "WebApplicationContext:Action@classpath:spring-web-context.xml")

    addListener(new ContextListener)
    sc.addListener(new HttpSessionEventPublisher)
    sc.addServlet("Action", new DispatcherServlet).addMapping("/*")
    sc.addServlet("StaticResource", new StaticResourceServlet()).addMapping("/static/*")
  }
}