package org.beangle.webmvc.struts2.dispatcher

import org.apache.struts2.dispatcher.mapper.{ActionMapper, ActionMapping, DefaultActionMapper}
import org.beangle.commons.web.util.RequestUtils

import com.opensymphony.xwork2.config.ConfigurationManager

import javax.servlet.http.HttpServletRequest

class ConventionActionMapper extends DefaultActionMapper with ActionMapper {
  val DefaultMethod = "index"

  protected def parseNameAndNamespace(uri: String, mapping: ActionMapping): ActionMapping = {
    val lastSlash = uri.lastIndexOf("/")
    val data =
      if (lastSlash == -1) {
        ("", uri)
      } else if (lastSlash == 0) {
        ("/", uri.substring(lastSlash + 1))
      } else {
        // Simply select the namespace as everything before the last slash
        (uri.substring(0, lastSlash), uri.substring(lastSlash + 1))
      }
    val namespace = data._1
    val name = data._2

    // process ! . 
    var i = 0
    var bangIdx = -1
    var lastIdx = name.length
    val chars = new Array[Char](name.length)
    name.getChars(0, name.length, chars, 0)
    var continue = true
    while (i < chars.length && continue) {
      var c = chars(i)
      if ('!' == c) bangIdx = i
      else if (';' == c || '.' == c) {
        lastIdx = i
        continue = false
      }
      i += 1
    }

    mapping.setNamespace(namespace)
    if (-1 == bangIdx) {
      mapping.setName(name.substring(0, lastIdx))
      mapping.setMethod(DefaultMethod)
    } else {
      mapping.setName(name.substring(0, bangIdx))
      mapping.setMethod(name.substring(bangIdx + 1, lastIdx))
    }
    mapping
  }

  /**
   * reserved method parameter
   */
  override def getMapping(request: HttpServletRequest, configManager: ConfigurationManager): ActionMapping = {
    parseNameAndNamespace(RequestUtils.getServletPath(request), new ActionMapping())
  }
}