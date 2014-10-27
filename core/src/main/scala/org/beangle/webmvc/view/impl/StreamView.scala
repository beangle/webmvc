package org.beangle.webmvc.view.impl

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.{ StreamView, View }
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.view.ViewRender

@description("流视图渲染者")
class StreamViewRender(configurer: Configurer) extends ViewRender with Logging {

  override def supportViewClass: Class[_] = {
    classOf[StreamView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val stream = view.asInstanceOf[StreamView]
    try {
      val response = context.response
      response.reset()
      response.setContentType(stream.contentType)
      val encodeName = RequestUtils.encodeAttachName(context.request, stream.displayName)
      response.setHeader("Content-Disposition", "attachment; filename=" + encodeName)
      response.setHeader("Location", encodeName)

      IOs.copy(stream.inputStream, response.getOutputStream)
    } catch {
      case e: Exception => warn(s"download file error ${stream.displayName}", e)
    } finally {
      IOs.close(stream.inputStream)
    }
  }
}
