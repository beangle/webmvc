package org.beangle.webmvc.struts2

import org.apache.struts2.dispatcher.ng.servlet.StrutsServlet
import org.beangle.commons.web.filter.CharacterEncodingFilter
import org.beangle.commons.web.resource.StaticResourceServlet
import org.beangle.commons.web.session.HttpSessionEventPublisher
import org.beangle.webmvc.struts2.config.PropertyConstantProvider

import javax.servlet.ServletContext

class Initializer extends org.beangle.commons.web.init.Initializer {
  override def onStartup(sc: ServletContext) {
    sc.setInitParameter("templatePath", "webapp://pages,class://")
    sc.addListener(new HttpSessionEventPublisher)
    sc.addFilter("CharacterEncoding", new CharacterEncodingFilter).setInitParameter("forceEncoding", "true")
    sc.addServlet("StaticResource", new StaticResourceServlet).addMapping("/static/*")
    val sr = sc.addServlet("Action", new StrutsServlet)
    sr.setInitParameter("configProviders", classOf[PropertyConstantProvider].getName)
    sr.addMapping("/*")
  }
}