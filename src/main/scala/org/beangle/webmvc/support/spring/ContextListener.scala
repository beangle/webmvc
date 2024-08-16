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

import jakarta.servlet.{ServletContext, ServletContextEvent, ServletContextListener}
import org.beangle.cdi.Container
import org.beangle.cdi.spring.context.DefaultContextInitializer
import org.beangle.commons.logging.Logging

/**
 * 1. Disable Definition Overriding
 * 2. Default config location(spring-context.xml)
 */
class ContextListener extends ServletContextListener with Logging {

  private var initializer: DefaultContextInitializer = _

  def loadContainer(sc: ServletContext): Container = {
    val contextConfigLocation = sc.getInitParameter("contextConfigLocation")
    val contextClassName = sc.getInitParameter("contextClassName")
    this.initializer = DefaultContextInitializer(contextConfigLocation, contextClassName)
    this.initializer.init()
  }

  override def contextInitialized(sce: ServletContextEvent): Unit = {
    if (null == initializer) loadContainer(sce.getServletContext)
  }

  override def contextDestroyed(sce: ServletContextEvent): Unit = {
    if null != initializer then initializer.close()
  }
}
