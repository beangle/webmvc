package org.beangle.webmvc.view.tag

import org.beangle.webmvc.view.bean.{ DefaultActionUriRender, IndexableIdGenerator }
import org.beangle.webmvc.view.component.ComponentContext
import org.beangle.webmvc.view.freemarker.FreemarkerTemplateEngine
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.inject.Containers

/**
 * Beangle tag Library
 *
 * @author chaostone
 * @since 2.0
 */
class BeangleTagLibrary extends TagLibrary {

  var suffix: String = _

  def getModels(req: HttpServletRequest, res: HttpServletResponse) = {
    val queryString = req.getQueryString
    val fullpath = if (null == queryString) req.getRequestURI() else req.getRequestURI() + queryString
    val idGenerator = new IndexableIdGenerator(String.valueOf(Math.abs(fullpath.hashCode)))

    val uriRender = new DefaultActionUriRender(suffix)
    val templateEngine = Containers.root.getBean(classOf[FreemarkerTemplateEngine]).orNull
    val componentContext = new ComponentContext(uriRender, idGenerator, templateEngine)
    new BeangleModels(componentContext, req)
  }

}