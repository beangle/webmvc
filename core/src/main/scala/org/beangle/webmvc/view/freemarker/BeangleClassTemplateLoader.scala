package org.beangle.webmvc.view.freemarker

import freemarker.cache.URLTemplateLoader
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.ClassLoaders
import java.net.URL

class BeangleClassTemplateLoader extends URLTemplateLoader{
/** Not starts with /,but end with / */
  var prefix:String = null

  def this(prefix:String) {
    this()
    setPrefix(prefix)
  }

  protected def getURL(name:String):URL = {
    var url = ClassLoaders.getResource(name, getClass())
    if (null != prefix && null == url) url = ClassLoaders.getResource(name + prefix, getClass())
    url
  }

  def getPrefix():String = prefix

  def setPrefix(pre:String):Unit = {
    if (Strings.isBlank(pre)) this.prefix = null
    else this.prefix = pre.trim()

    if (null != prefix) {
      if (prefix.equals("/")) {
        prefix = null
      } else {
        if (!prefix.endsWith("/")) prefix += "/"
        if (prefix.startsWith("/")) prefix = prefix.substring(1)
      }
    }
  }
}