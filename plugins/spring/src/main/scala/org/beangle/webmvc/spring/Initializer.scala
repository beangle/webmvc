/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.spring

import org.beangle.commons.event.EventMulticaster
import org.beangle.commons.lang.SystemInfo
import org.beangle.commons.web.session.HttpSessionEventPublisher
import org.beangle.cdi.spring.web.ContextListener
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.ActionContextBuilder
import org.beangle.webmvc.dispatch.Dispatcher
import org.beangle.webmvc.dispatch.RequestMapper

import javax.servlet.MultipartConfigElement
import javax.servlet.ServletContext

class Initializer extends org.beangle.commons.web.init.Initializer {

  override def onStartup(sc: ServletContext) {
    sc.setInitParameter("templatePath", "class://")

    val ctxListener = new ContextListener
    ctxListener.childContextConfigLocation = "WebApplicationContext:Action@classpath:spring-web-context.xml"
    val container = ctxListener.loadContainer()
    addListener(ctxListener)

    container.getBean(classOf[EventMulticaster]) foreach { em =>
      sc.addListener(new HttpSessionEventPublisher(em))
    }

    val action = sc.addServlet("Action", new Dispatcher(container))
    action.addMapping("/*")
    action.setMultipartConfig(new MultipartConfigElement(SystemInfo.tmpDir))
  }
}
