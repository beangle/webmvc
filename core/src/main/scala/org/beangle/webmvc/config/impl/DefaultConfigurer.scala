package org.beangle.webmvc.config.impl

import org.beangle.commons.logging.Logging
import org.beangle.webmvc.config.{ Configurer, Profile }

class DefaultConfigurer extends Configurer with Logging {

  private val class2Profiles = new collection.mutable.HashMap[String, Profile]

  private val profiles = (new XmlProfileProvider).loadProfiles()

  def getProfile(className: String): Profile = {
    var matched = class2Profiles.get(className).orNull
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
      class2Profiles.put(className, matched)
      debug(s"${className} match profile:${matched}")
    }
    matched
  }

}
