package org.beangle.webmvc.api.action

import java.net.URL

import org.beangle.commons.http.accept.ContentNegotiationManager
import org.beangle.commons.lang.{Chars, ClassLoaders, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.{CookieUtils, RequestUtils}
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.{ActionMessages, ContextHolder, Flash}
import org.beangle.webmvc.api.view.{ForwardActionView, RedirectActionView, View}

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

abstract class MimeSupport {

  var contentNegotiationManager: ContentNegotiationManager = _

  def isRequestCsv: Boolean = {
    if (null == contentNegotiationManager) false
    else {
      contentNegotiationManager.resolve(ContextHolder.context.request).exists { p => p.getBaseType == "text/csv" }
    }
  }
}