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
package org.beangle.webmvc.view.impl

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.{ StreamView, View }
import org.beangle.webmvc.view.ViewRender

@description("流视图渲染者")
class StreamViewRender extends ViewRender with Logging {

  override def supportViewClass: Class[_] = {
    classOf[StreamView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val stream = view.asInstanceOf[StreamView]
    try {
      val response = context.response
      response.setContentType(stream.contentType)
      RequestUtils.setContentDisposition(response, stream.displayName)
      IOs.copy(stream.inputStream, response.getOutputStream)
    } catch {
      case e: Exception => logger.warn(s"download file error ${stream.displayName}", e)
    } finally {
      IOs.close(stream.inputStream)
    }
  }
}
