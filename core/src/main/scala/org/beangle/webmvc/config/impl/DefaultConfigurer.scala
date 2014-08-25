package org.beangle.webmvc.config.impl

import org.beangle.commons.logging.Logging
import org.beangle.webmvc.config.{ Configurer, Profile }
import org.beangle.webmvc.config.ProfileProvider
import org.beangle.commons.lang.annotation.description

@description("缺省配置器")
class DefaultConfigurer(profileProvider: ProfileProvider) extends Configurer with Logging {

  private val class2Profiles = new collection.mutable.HashMap[String, Profile]

  val profiles = profileProvider.loadProfiles()

  def getProfile(className: String): Profile = {
    var matched = class2Profiles.get(className).orNull
    if (null != matched) return matched
    profiles.find(p => !p.matches(className).isEmpty) match {
      case Some(p) =>
        class2Profiles.put(className, p)
        matched = p
        debug(s"${className} match profile:${p}")
      case None =>
    }
    matched
  }

}
