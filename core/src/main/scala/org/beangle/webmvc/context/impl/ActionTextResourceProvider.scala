package org.beangle.webmvc.context.impl

import java.{ util => ju }

import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.{ TextBundleRegistry, TextFormater, TextResource, TextResourceProvider }
import org.beangle.webmvc.api.context.ContextHolder

class ActionTextResourceProvider(registry: TextBundleRegistry, formater: TextFormater)
  extends TextResourceProvider with Initializing {

  var defaults: String = "beangle,application"

  override def init(): Unit = {
    registry.addDefaults(Strings.split(defaults, ","): _*)
  }

  def getTextResource(locale: ju.Locale): TextResource = {
    new ActionTextResource(ContextHolder.context, locale, registry, formater)
  }
}