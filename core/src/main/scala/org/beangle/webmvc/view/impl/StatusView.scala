package org.beangle.webmvc.view.impl

import org.beangle.webmvc.view.ViewRender
import org.beangle.commons.logging.Logging
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.view.StatusView
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View

@description("HTTP状态渲染者")
class StatusViewRender extends ViewRender with Logging {
  override def supportViewClass: Class[_] = {
    classOf[StatusView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    context.response.setStatus(view.asInstanceOf[StatusView].code)
  }
}