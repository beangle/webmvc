package org.beangle.webmvc.struts2.dispatcher

import org.apache.struts2.dispatcher.mapper.{ ActionMapper, ActionMapping, DefaultActionMapper }
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.route.impl.DefaultURIResolver

import com.opensymphony.xwork2.config.ConfigurationManager

import javax.servlet.http.HttpServletRequest

class ConventionActionMapper extends DefaultActionMapper with ActionMapper {
  val resolver = new DefaultURIResolver
  /**
   * reserved method parameter
   */
  override def getMapping(request: HttpServletRequest, configManager: ConfigurationManager): ActionMapping = {
    val m = resolver.resolve(request)
    val am = new ActionMapping()
    am.setNamespace(m.namespace)
    am.setName(m.name)
    am.setMethod(m.method)
    am
  }
}