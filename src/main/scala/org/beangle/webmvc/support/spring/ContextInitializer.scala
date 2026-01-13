/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.support.spring

import jakarta.servlet.{MultipartConfigElement, ServletContext, ServletContextEvent, ServletContextListener}
import org.beangle.cdi.spring.context.ContextInitializer as SpringContextInitializer
import org.beangle.commons.cdi.Container
import org.beangle.commons.lang.{Objects, SystemInfo}
import org.beangle.web.servlet.init.Initializer
import org.beangle.webmvc.config.Configurator
import org.beangle.webmvc.context.ActionContextBuilder
import org.beangle.webmvc.dispatch.{Dispatcher, ExceptionHandler, RequestMapper}
import org.beangle.webmvc.view.Static

class ContextInitializer extends Initializer, ServletContextListener {

  private var initializer: SpringContextInitializer = _

  private def loadContainer(sc: ServletContext): Container = {
    val contextConfigLocation = Objects.nvl(sc.getInitParameter("contextConfigLocation"), "classpath:spring-context.xml")
    val contextClassName = sc.getInitParameter("contextClassName")
    this.initializer = SpringContextInitializer(contextConfigLocation, contextClassName)
    this.initializer.init()
  }

  override def onStartup(sc: ServletContext): Unit = {
    initStaticBase(sc)
    val container = loadContainer(sc)
    val cfg = container.getBean(classOf[Configurator]).get
    val mapper = container.getBean(classOf[RequestMapper]).get
    val exceptionHandler = container.getBean(classOf[ExceptionHandler]).get
    val ctxBuilder = container.getBean(classOf[ActionContextBuilder]).get

    val action = sc.addServlet("Action", new Dispatcher(cfg, mapper, exceptionHandler, ctxBuilder))
    action.addMapping("/*")
    action.setMultipartConfig(new MultipartConfigElement(SystemInfo.tmpDir))
    addListener(this)
  }

  def initStaticBase(context: ServletContext): Unit = {
    if (null == Static.Default.base) {
      val p = System.getProperty("beangle.webmvc.static_base")
      Static.Default.base = if (null == p) context.getContextPath + "/static" else p
    }
  }

  override def contextInitialized(sce: ServletContextEvent): Unit = {
  }

  override def contextDestroyed(sce: ServletContextEvent): Unit = {
    if null != initializer then initializer.close()
  }
}
