package org.beangle.webmvc.view.bean

import org.beangle.commons.lang.Strings
import org.beangle.commons.web.url.UrlRender
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.config.ActionMapping
import org.beangle.commons.lang.time.Stopwatch

/**
 * Url render
 *
 * @author chaostone
 * @since 2.4
 */
trait ActionUriRender {
  def render(action: ActionMapping, uri: String): String
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

  override def render(action: ActionMapping, uri: String): String = {
    val context = ContextHolder.context.request.getServletContext().getContextPath
    val newUri = if (uri.charAt(0) == '!') action.name + uri.replace('!', '/') else uri.replace('!', '/')
    render.render(context, action.namespace + "/" + action.name, newUri)
  }

}