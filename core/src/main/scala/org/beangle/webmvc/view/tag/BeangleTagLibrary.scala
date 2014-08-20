package org.beangle.webmvc.view.tag

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.helper.ContainerHelper
import org.beangle.webmvc.view.component.ComponentContext
import org.beangle.webmvc.view.freemarker.FreemarkerTemplateEngine
import org.beangle.webmvc.view.impl.{DefaultActionUriRender, IndexableIdGenerator}

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
 * Beangle tag Library
 *
 * @author chaostone
 * @since 2.0
 */
class BeangleTagLibrary extends TagLibrary {

  var suffix: String = _

  def getModels(req: HttpServletRequest, res: HttpServletResponse): AnyRef = {
    val queryString = req.getQueryString
    val fullpath = if (null == queryString) req.getRequestURI() else req.getRequestURI() + queryString
    val idGenerator = new IndexableIdGenerator(String.valueOf(Math.abs(fullpath.hashCode)))

    val uriRender = new DefaultActionUriRender(suffix)
    val templateEngine = ContainerHelper.get.getBean(classOf[FreemarkerTemplateEngine]).orNull
    val componentContext = new ComponentContext(uriRender, idGenerator, templateEngine)
    new BeangleModels(componentContext, req)
  }

}