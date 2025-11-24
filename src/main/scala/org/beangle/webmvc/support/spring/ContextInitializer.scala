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

import jakarta.servlet.{MultipartConfigElement, ServletContext}
import org.beangle.commons.lang.SystemInfo
import org.beangle.web.servlet.init.Initializer
import org.beangle.webmvc.config.Configurator
import org.beangle.webmvc.context.ActionContextBuilder
import org.beangle.webmvc.dispatch.{Dispatcher, ExceptionHandler, RequestMapper}
import org.beangle.webmvc.view.Static

class ContextInitializer extends Initializer {

  override def onStartup(sc: ServletContext): Unit = {
    initStaticBase(sc)

    val ctxListener = new ContextListener
    val container = ctxListener.loadContainer(sc)
    addListener(ctxListener)

    val cfg = container.getBean(classOf[Configurator]).get
    val mapper = container.getBean(classOf[RequestMapper]).get
    val exceptionHandler = container.getBean(classOf[ExceptionHandler]).get
    val ctxBuilder = container.getBean(classOf[ActionContextBuilder]).get

    val action = sc.addServlet("Action", new Dispatcher(cfg, mapper, exceptionHandler, ctxBuilder))
    action.addMapping("/*")
    action.setMultipartConfig(new MultipartConfigElement(SystemInfo.tmpDir))
  }

  def initStaticBase(context: ServletContext): Unit = {
    if (null == Static.Default.base) {
      val p = System.getProperty("beangle.webmvc.static_base")
      Static.Default.base = if (null == p) context.getContextPath + "/static" else p
    }
  }
}
