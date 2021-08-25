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

package org.beangle.webmvc.showcase.action.config

import java.net.URL

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.annotation.description
import org.beangle.web.action.support.ActionSupport
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.view.View

@description("Web初始化配置查看器")
class WebinitAction extends ActionSupport {

  def index(): View = {
    val context = ActionContext.current.request.getServletContext
    val url = context.getResource("/WEB-INF/web.xml")
    if (null != url) put("webxml", IOs.readString(url.openStream))
    val initializers = new collection.mutable.HashMap[String, URL]

    val initURLs = ClassLoaders.getResources("META-INF/beangle/web-init.properties")
    initURLs foreach { url =>
      IOs.readJavaProperties(url) get ("initializer") match {
        case Some(clazz) => initializers.put(clazz, url)
        case None =>
      }
    }
    put("initializers", initializers)
    forward()
  }
}
