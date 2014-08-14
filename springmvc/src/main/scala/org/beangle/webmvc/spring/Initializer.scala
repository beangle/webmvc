package org.beangle.webmvc.spring

import javax.servlet.ServletContext
import org.springframework.web.servlet.DispatcherServlet
import org.beangle.commons.web.session.HttpSessionEventPublisher
import org.beangle.commons.web.filter.CharacterEncodingFilter

class Initializer extends org.beangle.commons.web.init.Initializer {
  override def onStartup(sc: ServletContext) {
    sc.setInitParameter("templatePath", "webapp://pages,class://")
    sc.addListener(new HttpSessionEventPublisher)
    sc.addFilter("CharacterEncoding", new CharacterEncodingFilter).setInitParameter("forceEncoding", "true")

    val sr = sc.addServlet("Action", new DispatcherServlet)
    sr.setInitParameter("contextAttribute", "WebApplicationContext:Action")
    sr.addMapping("/*")
  }
}