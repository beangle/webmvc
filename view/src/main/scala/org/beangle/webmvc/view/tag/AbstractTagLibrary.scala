package org.beangle.webmvc.view.tag

import org.beangle.webmvc.view.TagLibrary
import org.beangle.webmvc.dispatch.ActionUriRender
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.view.impl.IndexableIdGenerator

/**
 * @author chaostone
 */
abstract class AbstractTagLibrary extends TagLibrary {

  var uriRender: ActionUriRender = _
  var templateEngine: TemplateEngine = _

  protected def buildComponentContext(req: HttpServletRequest): ComponentContext = {
    val queryString = req.getQueryString
    val fullpath = if (null == queryString) req.getRequestURI() else req.getRequestURI() + queryString
    val idGenerator = new IndexableIdGenerator(String.valueOf(Math.abs(fullpath.hashCode)))
    new ComponentContext(uriRender, idGenerator, templateEngine)
  }
}