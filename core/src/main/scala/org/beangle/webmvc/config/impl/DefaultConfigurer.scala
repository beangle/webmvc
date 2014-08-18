package org.beangle.webmvc.config.impl

import java.lang.reflect.Method
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.config.ActionMapping
import org.beangle.webmvc.config.Profile
import org.beangle.webmvc.api.action.ToStruts
import org.beangle.webmvc.config.Configurer

class DefaultConfigurer extends Configurer with Logging {

  private val classProfiles = new collection.mutable.HashMap[String, Profile]

  private val actionMappingBuilder = new DefaultActionMappingBuilder

  private val profiles = (new XmlProfileProvider).loadProfiles()

  def getProfile(className: String): Profile = {
    var matched = classProfiles.get(className).orNull
    if (null != matched) return matched
    var index: Int = -1
    var patternLen: Int = 0
    for (profile <- profiles) {
      profile.matches(className) foreach { matcheInfo =>
        val newIndex = matcheInfo.start
        if (newIndex >= index && profile.actionPattern.length >= patternLen) {
          matched = profile
          index = newIndex
          patternLen = profile.actionPattern.length
        }
      }
    }
    if (null != matched) {
      classProfiles.put(className, matched)
      debug(s"${className} match profile:${matched}")
    }
    matched
  }

  def buildMappings(clazz: Class[_]): Seq[Tuple2[ActionMapping, Method]] = {
    actionMappingBuilder.build(clazz, getProfile(clazz.getName))
  }
}
