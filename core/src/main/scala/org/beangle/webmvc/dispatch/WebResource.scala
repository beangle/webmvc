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

package org.beangle.webmvc.dispatch

import jakarta.servlet.ServletContext
import jakarta.servlet.http.HttpServletResponse
import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.Strings.{isNotEmpty, substringAfterLast}

object WebResource {

  def deliver(response: HttpServletResponse, ctx: ServletContext, path: String): Unit = {
    var url = ctx.getResource(path)
    if (null == url) {
      notfound(response, path)
    } else {
      var is = url.openStream()
      if (null == is) {
        import scala.jdk.javaapi.CollectionConverters._
        val subPaths = asScala(ctx.getResourcePaths(path))
        subPaths.find(_.contains("/index.htm")) foreach { subIndex =>
          url = ctx.getResource(subIndex)
          is = url.openStream()
        }
      }
      if (null == is) {
        notfound(response, path)
      } else {
        val ext = substringAfterLast(url.getFile, ".")
        if (isNotEmpty(ext)) MediaTypes.get(ext) foreach (m => response.setContentType(m.toString))
        IOs.copy(is, response.getOutputStream)
      }
    }
  }

  private def notfound(response: HttpServletResponse, path: String): Unit = {
    response.getWriter.write(s"HTTP 404:Cannot find $path")
    response.setStatus(HttpServletResponse.SC_NOT_FOUND)
  }
}
