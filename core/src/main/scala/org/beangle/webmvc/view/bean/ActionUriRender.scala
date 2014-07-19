package org.beangle.webmvc.view.bean

import org.beangle.commons.web.url.UrlRender
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.context.ContextHolder

/**
 * Url render
 *
 * @author chaostone
 * @since 2.4
 */
trait ActionUriRender {
  def render(referer: String, uri: String): String
}

object DefaultActionUriRender {
  private def buildUrlRender(suffix: String): UrlRender = {
    val firstSuffix: String = {
      if (Strings.isNotEmpty(suffix)) {
        val commaIndex = suffix.indexOf(",")
        if (-1 == commaIndex) suffix
        else suffix.substring(0, commaIndex)
      } else null
    }
    new UrlRender(firstSuffix)
  }
}

class DefaultActionUriRender(suffix: String) extends ActionUriRender {

  val render = DefaultActionUriRender.buildUrlRender(suffix)

  def render(referer: String, uri: String): String = {
    val context = ContextHolder.context.request.getServletContext().getContextPath
    render.render(context, referer, uri)
  }
}