package org.beangle.webmvc.context

import java.{ util => ju }
import org.beangle.commons.bean.Initializing
import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.text.i18n.spi.{ TextBundleRegistry, TextFormater, TextResourceProvider }
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.commons.lang.Strings

class ActionTextResourceProvider(registry: TextBundleRegistry, formater: TextFormater) extends TextResourceProvider with Initializing {

  var defaults: String = "beangle,application"

  override def init(): Unit = {
    registry.addDefaults(Strings.split(defaults, ","): _*)
  }

  def getTextResource(locale: ju.Locale): TextResource = {
    new ActionTextResource(ContextHolder.context, locale, registry, formater)
  }
}