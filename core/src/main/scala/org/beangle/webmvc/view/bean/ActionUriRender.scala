package org.beangle.webmvc.view.bean

import org.beangle.commons.lang.Strings
import org.beangle.commons.web.url.UrlRender
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.route.ActionMapping

/**
 * Url render
 *
 * @author chaostone
 * @since 2.4
 */
trait ActionUriRender {
  def render(mapping: ActionMapping, uri: String): String
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

  override def render(mapping: ActionMapping, uri: String): String = {
    val context = ContextHolder.context.request.getServletContext().getContextPath
    val newUri = if (uri.charAt(0) == '!') mapping.name + "/" + uri.substring(1) else uri.replace('!', '/')
    render.render(context, mapping.namespace + "/" + mapping.name, newUri)
  }
}