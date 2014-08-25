package org.beangle.webmvc

import org.beangle.commons.web.filter.CharacterEncodingFilter
import org.beangle.commons.web.session.HttpSessionEventPublisher
import org.beangle.webmvc.dispatch.DispatcherServlet
import javax.servlet.ServletContext
import org.beangle.commons.web.resource.StaticResourceServlet

/**
 * FIXME move in sigle initializer module
 */
class Initializer extends org.beangle.commons.web.init.Initializer {
  override def onStartup(sc: ServletContext) {
    sc.setInitParameter("templatePath", "webapp://pages,class://")
    sc.addListener(new HttpSessionEventPublisher)
    sc.addFilter("CharacterEncoding", new CharacterEncodingFilter)

    val sr = sc.addServlet("Action", new DispatcherServlet)
    sr.setInitParameter("contextAttribute", "WebApplicationContext:Action")
    sr.addMapping("/*")

    sc.addServlet("StaticResource", new StaticResourceServlet()).addMapping("/static/*")
  }
}