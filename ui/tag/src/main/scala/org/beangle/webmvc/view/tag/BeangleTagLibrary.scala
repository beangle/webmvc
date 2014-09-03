package org.beangle.webmvc.view.tag

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.view.impl.IndexableIdGenerator
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.view.TagLibrary

/**
 * Beangle tag Library
 *
 * @author chaostone
 * @since 2.0
 */
@description("beangle标签库")
class BeangleTagLibrary extends TagLibrary {

  var uriRender: ActionUriRender = _
  var templateEngine: TemplateEngine = _

  def getModels(req: HttpServletRequest, res: HttpServletResponse): AnyRef = {
    val queryString = req.getQueryString
    val fullpath = if (null == queryString) req.getRequestURI() else req.getRequestURI() + queryString
    val idGenerator = new IndexableIdGenerator(String.valueOf(Math.abs(fullpath.hashCode)))

    val componentContext = new ComponentContext(uriRender, idGenerator, templateEngine)
    new BeangleModels(componentContext, req)
  }

}