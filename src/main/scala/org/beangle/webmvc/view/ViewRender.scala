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

package org.beangle.webmvc.view

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.{description, spi}
import org.beangle.webmvc.context.ActionContext
import org.beangle.web.servlet.util.RequestUtils

@spi
trait ViewRender {
  def supportViewClass: Class[_]

  def render(view: View, context: ActionContext): Unit
}

@description("原始数据渲染者")
class RawViewRender extends ViewRender {
  override def supportViewClass: Class[_] = {
    classOf[RawView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val data = view.asInstanceOf[RawView].data
    val res = context.response
    if (data.getClass.isArray && data.getClass.getComponentType == classOf[Byte]) {
      res.getOutputStream.write(data.asInstanceOf[Array[Byte]])
    } else {
      res.setCharacterEncoding("UTF-8")
      res.getWriter.write(data.toString)
    }
  }
}

@description("HTTP状态渲染者")
class StatusViewRender extends ViewRender {
  override def supportViewClass: Class[_] = {
    classOf[StatusView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    context.response.setStatus(view.asInstanceOf[StatusView].code)
  }
}

@description("流视图渲染者")
class StreamViewRender extends ViewRender {

  override def supportViewClass: Class[_] = {
    classOf[StreamView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val stream = view.asInstanceOf[StreamView]
    val response = context.response
    try {
      val contentType = stream.contentType.toString
      response.setContentType(contentType)
      if !contentType.startsWith("image/") then RequestUtils.setContentDisposition(response, stream.displayName)
      stream.lastModified foreach { lm =>
        response.addDateHeader("Last-Modified", lm)
      }
      val length = IOs.copy(stream.inputStream, response.getOutputStream)
      response.setContentLengthLong(length)
    } catch {
      case e: Exception =>
        if (!response.isCommitted)
          response.getWriter.write(s"download file error ${stream.displayName},for ${e.getMessage}")
    } finally {
      IOs.close(stream.inputStream)
      stream.postHook foreach (f => f())
    }
  }
}
