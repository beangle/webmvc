package org.beangle.webmvc.config.action

import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.context.ContextHolder
import java.io.File
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.io.IOs
import java.net.URL
import org.beangle.commons.lang.annotation.description

@description("Web初始化配置查看器")
class WebinitAction extends ActionSupport {

  def index(): String = {
    val context = ContextHolder.context.request.getServletContext
    val webxml = context.getRealPath("WEB-INF/web.xml")
    val url =
      if (null != webxml && new File(webxml).exists) {
        new File(webxml).toURI.toURL
      } else {
        ClassLoaders.getResource("WEB-INF/web.xml")
      }
    if (null != url) put("webxml", IOs.readString(url.openStream))
    val initializers = new collection.mutable.HashMap[String, URL]

    val initURLs = ClassLoaders.getResources("META-INF/beangle/web-init.properties")
    initURLs foreach { url =>
      IOs.readJavaProperties(url) get ("initializer") match {
        case Some(clazz) => initializers.put(clazz, url)
        case None =>
      }
    }
    put("initializers", initializers)
    forward()
  }
}