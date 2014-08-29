package org.beangle.webmvc.dispatch.impl

import org.beangle.commons.lang.Strings
import org.beangle.commons.web.url.UrlRender
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.config.ActionMapping
import org.beangle.commons.web.url.UrlBuilder
import org.beangle.webmvc.dispatch.RequestMapper
import org.beangle.webmvc.api.action.to
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.commons.lang.annotation.description

@description("根据uri相对地址反向生成绝对地址")
class DefaultActionUriRender extends ActionUriRender {

  val render = new UrlRender

  var mapper: RequestMapper = _

  override def render(action: ActionMapping, uri: String): String = {
    if (uri.charAt(0) == '/') return uri

    val context = ContextHolder.context
    var params: collection.mutable.Map[String, String] = null
    val config = action.config
    val mapping =
      if (uri.charAt(0) == '!') {
        var dotIdx = uriEndIndexOf(uri)
        params = to(uri).parameters
        config.mappings(uri.substring(1, dotIdx))
      } else {
        val name = config.name
        var backStep = 1
        var index = 0
        while (uri.startsWith("../", index)) {
          index += 3
          backStep += 1
        }
        var chars = new Array[Char](name.length)
        name.getChars(0, chars.length, chars, 0)
        var findedSlash = chars.length - 1
        while (backStep > 0 && findedSlash > -1) {
          if (chars(findedSlash) == '/') backStep -= 1
          findedSlash -= 1
        }

        val finalURL = name.substring(0, findedSlash + 2) + (if (index == 0) uri else uri.substring(index))
        val struts = to(finalURL).toStruts
        params = struts.parameters

        val actionName = new StringBuilder
        val namespace = struts.namespace
        if (null == namespace || namespace.length == 1) actionName.append('/')
        else actionName.append(namespace).append('/')
        if (null != struts.name) actionName.append(struts.name)
        mapper.antiResolve(actionName.toString) match {
          case Some(cfg) => cfg.mappings(if (null == struts.method) cfg.profile.defaultMethod else struts.method)
          case None => throw new RuntimeException(s"Cannot find $actionName mapping")
        }
      }
    val contextPath = context.request.getServletContext().getContextPath
    val tourl = mapping.toURL(params, context.params)
    params --= mapping.urlParams.values
    contextPath + tourl.params(params).url
  }

  /**
   * Return uri end index of url
   */
  def uriEndIndexOf(url: String): Int = {
    var lastIndex = url.length
    var chars = new Array[Char](lastIndex)
    url.getChars(0, lastIndex, chars, 0)
    var i = 0
    while (i < lastIndex) {
      var c = chars(i)
      if (c == '.' || c == '?') return i
      i += 1
    }
    lastIndex
  }
}