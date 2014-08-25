package org.beangle.webmvc.view.impl

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.api.action.{ ToClass, ToURL }
import org.beangle.webmvc.api.context.{ ActionContext, ContextHolder }
import org.beangle.webmvc.api.view.{ ActionView, ForwardActionView, RedirectActionView, View }
import org.beangle.webmvc.dispatch.RequestMapper
import org.beangle.webmvc.view.ViewRender
import org.beangle.webmvc.view.TypeViewBuilder
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.action.to
import org.beangle.commons.lang.annotation.description

@description("前向调转视图构建者")
class ForwardActionViewBuilder extends TypeViewBuilder {

  override def build(view: view): View = {
    new ForwardActionView(to(view.location))
  }

  override def supportViewType: String = {
    "chain"
  }
}

@description("重定向调转视图构建者")
class RedirectActionViewBuilder extends TypeViewBuilder {

  override def build(view: view): View = {
    new RedirectActionView(to(view.location))
  }

  override def supportViewType: String = {
    "redirectAction"
  }
}

abstract class ActionViewRender(val mapper: RequestMapper) extends ViewRender {

  final def toURL(view: View): String = {
    view.asInstanceOf[ActionView].to match {
      case ca: ToClass =>
        mapper.antiResolve(ca.clazz.getName, ca.method) match {
          case Some(am) =>
            val ua = am.toURL(ca.parameters, ContextHolder.context.params)
            ca.parameters --= am.urlParams.values
            ua.params(ca.parameters)
            ua.url
          case None => throw new RuntimeException(s"Cannot find action mapping for ${ca.clazz.getName} ${ca.method}")
        }
      case ua: ToURL => ua.url
    }
  }
}
@description("前向调转渲染者")
class ForwardActionViewRender(mapper: RequestMapper) extends ActionViewRender(mapper) {

  override def supportViewClass: Class[_] = {
    classOf[ForwardActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    context.request.getRequestDispatcher(toURL(view)).forward(context.request, context.response)
  }
}

@description("重定向调转渲染者")
class RedirectActionViewRender(mapper: RequestMapper) extends ActionViewRender(mapper) {

  override def supportViewClass: Class[_] = {
    classOf[RedirectActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val request = context.request
    val response = context.response
    val url = new StringBuilder(toURL(view))

    var redirectParams = request.getParameter("_params")
    val ajaxHead = "x-requested-with"
    if (null != redirectParams) {
      if (redirectParams.charAt(0) == '&') redirectParams = redirectParams.substring(1)
      if (null != request.getHeader(ajaxHead)) redirectParams += "&x-requested-with=1"
    } else if (null != request.getHeader(ajaxHead)) redirectParams = "x-requested-with=1"

    if (null != redirectParams && url.contains("?")) {
      url.append("&").append(redirectParams)
    } else {
      url.append("?").append(redirectParams)
    }
    val finalLocation = if (request.getContextPath.length > 1) request.getContextPath + url.toString else url.toString
    val encodedLocation = response.encodeRedirectURL(finalLocation)

    response.sendRedirect(encodedLocation)
  }
}