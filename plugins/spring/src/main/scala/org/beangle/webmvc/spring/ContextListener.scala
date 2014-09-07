/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.spring

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Objects }
import org.beangle.commons.lang.Strings.{ substringAfter, substringBefore }
import org.beangle.commons.logging.Logging
import org.beangle.spring.context.{ ContextLoader, XmlWebApplicationContext }
import org.springframework.context.{ ApplicationContext, ConfigurableApplicationContext }
import javax.servlet.{ ServletContext, ServletContextEvent, ServletContextListener }
import org.beangle.commons.web.context.ServletContextHolder

/**
 * 1. Disable Definition Overriding
 * 2. Default config location(spring-context.xml)
 * 3. Load children context
 * 4. Store SerlvetContext in SerlvetContext
 */
class ContextListener extends ServletContextListener with Logging {

  val rootContextAttribute = "org.springframework.web.context.WebApplicationContext.ROOT"

  override def contextInitialized(sce: ServletContextEvent) {
    val sc = sce.getServletContext
    ServletContextHolder.store(sc)
    require(getContext(sc).isEmpty, "Cannot initialize context because there is already a root application context present!")
    val contextClass = determineContextClass(sc)
    val loader = new ContextLoader(contextClass, null)
    val configLocation = sc.getInitParameter("contextConfigLocation")
    val wac = loader.load("WebApplicationContext:ROOT", configLocation, Map(("displayName", "Root WebApplicationContext")))
    sc.setAttribute(rootContextAttribute, wac)

    //load children
    val childLocation = sc.getInitParameter("childContextConfigLocation")
    loadChildren(substringBefore(childLocation, "@"), substringAfter(childLocation, "@"), sc, wac, contextClass)
  }

  def loadChildren(id: String, configLocation: String, sc: ServletContext, parent: ApplicationContext, contextClass: Class[_]) {
    if (null != sc.getAttribute(id)) return
    sc.setAttribute(id, new ContextLoader(contextClass, parent).load(id, configLocation, Map(("displayName", "Action WebApplicationContext"))))
  }

  private def getContext(servletContext: ServletContext): Option[ConfigurableApplicationContext] = {
    servletContext.getAttribute(rootContextAttribute) match {
      case cac: ConfigurableApplicationContext => Some(cac)
      case _ => None
    }
  }

  override def contextDestroyed(sce: ServletContextEvent) {
    getContext(sce.getServletContext) match {
      case Some(context) =>
        context.close(); sce.getServletContext.removeAttribute(rootContextAttribute)
      case None =>
    }
  }

  private def determineContextClass(servletContext: ServletContext): Class[_] = {
    val ctxClassName = servletContext.getInitParameter("contextClass")
    if (ctxClassName != null) {
      ClassLoaders.loadClass(ctxClassName)
    } else {
      val propUrl = ClassLoaders.getResource("org/springframework/web/context/ContextLoader.properties")
      if (null != propUrl) {
        val properties = IOs.readJavaProperties(propUrl)
        ClassLoaders.loadClass(properties("org.springframework.web.context.WebApplicationContext"))
      } else
        classOf[XmlWebApplicationContext]
    }
  }
}
