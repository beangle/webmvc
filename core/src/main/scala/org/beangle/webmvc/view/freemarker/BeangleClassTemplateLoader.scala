package org.beangle.webmvc.view.freemarker

import freemarker.cache.URLTemplateLoader
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.ClassLoaders
import java.net.URL

object PrefixProcessor {

  /** Not starts with /,but end with / */
  def process(pre: String): String = {
    if (Strings.isBlank(pre)) return null
    var prefix = pre.trim()

    if (prefix.equals("/")) {
      null
    } else {
      if (!prefix.endsWith("/")) prefix += "/"
      if (prefix.startsWith("/")) prefix = prefix.substring(1)
      prefix
    }
  }
}

class BeangleClassTemplateLoader(prefixStr: String = null) extends URLTemplateLoader {

  val prefix = PrefixProcessor.process(prefixStr)

  protected def getURL(name: String): URL = {
    var url = ClassLoaders.getResource(name)
    if (null != prefix && null == url) url = ClassLoaders.getResource(name + prefix)
    url
  }

}