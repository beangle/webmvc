package org.beangle.webmvc.view.impl

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.api.action.{ ToClass, ToURI }
import org.beangle.webmvc.api.context.{ ActionContext, ContextHolder }
import org.beangle.webmvc.api.view.{ ActionView, ForwardActionView, RedirectActionView, View }
import org.beangle.webmvc.dispatch.RequestMapper
import org.beangle.webmvc.view.ViewRender

abstract class ActionViewRender(val mapper: RequestMapper) extends ViewRender {

  final def toURL(view: View): String = {
    view.asInstanceOf[ActionView].to match {
      case ca: ToClass =>
        mapper.antiResolve(ca.clazz, ca.method) match {
          case Some(rm) => rm.action.toURI(ca, ContextHolder.context.params).url
          case None => throw new RuntimeException(s"Cannot find action mapping for ${ca.clazz.getName} ${ca.method}")
        }
      case ua: ToURI => ua.url
    }
  }
}

class ForwardActionViewRender(mapper: RequestMapper) extends ActionViewRender(mapper) {

  override def supportView: Class[_] = {
    classOf[ForwardActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    context.request.getRequestDispatcher(toURL(view)).forward(context.request, context.response)
  }
}

class RedirectActionViewRender(mapper: RequestMapper) extends ActionViewRender(mapper) {

  override def supportView: Class[_] = {
    classOf[RedirectActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val request = context.request
    val response = context.response
    val url = toURL(view)
    val finalLocation = if (request.getContextPath.length > 1) request.getContextPath + url else url
    val encodedLocation = response.encodeRedirectURL(finalLocation)
    response.sendRedirect(encodedLocation)
  }
}