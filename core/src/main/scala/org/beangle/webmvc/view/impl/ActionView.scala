package org.beangle.webmvc.view.impl

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.api.action.{ ToClass, ToURI }
import org.beangle.webmvc.api.context.{ ActionContext, ContextHolder }
import org.beangle.webmvc.api.view.{ ActionView, ForwardActionView, RedirectActionView, View }
import org.beangle.webmvc.dispatch.RequestMapper
import org.beangle.webmvc.view.ViewRender
import org.beangle.webmvc.view.TypeViewBuilder
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.action.to

class ForwardActionViewBuilder extends TypeViewBuilder {

  override def build(view: view): View = {
    new ForwardActionView(to(view.location))
  }

  override def supportViewType: String = {
    "chain"
  }
}

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
        mapper.antiResolve(ca.clazz, ca.method) match {
          case Some(rm) => rm.action.toURI(ca, ContextHolder.context.params).url
          case None => throw new RuntimeException(s"Cannot find action mapping for ${ca.clazz.getName} ${ca.method}")
        }
      case ua: ToURI => ua.url
    }
  }
}

class ForwardActionViewRender(mapper: RequestMapper) extends ActionViewRender(mapper) {

  override def supportViewClass: Class[_] = {
    classOf[ForwardActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    context.request.getRequestDispatcher(toURL(view)).forward(context.request, context.response)
  }
}

class RedirectActionViewRender(mapper: RequestMapper) extends ActionViewRender(mapper) {

  override def supportViewClass: Class[_] = {
    classOf[RedirectActionView]
  }

  override def render(view: View, context: ActionContext): Unit = {
    val request = context.request
    val response = context.response
    val url = toURL(view)
    val finalLocation = if (request.getContextPath.length > 1) request.getContextPath + url else url
    val encodedLocation = response.encodeRedirectURL(finalLocation)
//     val redirectParamStrs = request.getParameterValues("params")
//        if (null != redirectParamStrs) {
//          for (redirectParamStr <- redirectParamStrs)
//            action.params(redirectParamStr)
//        }
//          // x-requested-with->XMLHttpRequest
//        if (null != request.getHeader("x-requested-with")) action.param("x-requested-with", "1")
//
//        val params = buildResultParams(path, cfg)
//        if (None != action.parameters.get("method")) {
//          params.put("method", action.parameters("method"))
//          action.parameters.remove("method")
//        }
    
    response.sendRedirect(encodedLocation)
  }
}