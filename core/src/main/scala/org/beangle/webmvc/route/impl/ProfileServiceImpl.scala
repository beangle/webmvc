package org.beangle.webmvc.route.impl

import java.net.URL
import java.{util => ju}

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.route.{Profile, ProfileService}

object ProfileServiceImpl extends Logging {

  val defaultProfile = loadDefaultProfile()

  /**
   * 初始化配置META-INF/convention-route.properties
   */
  def loadProfiles(): List[Profile] = {
    val profiles = new collection.mutable.ListBuffer[Profile]
    ClassLoaders.getResources("META-INF/beangle/convention-route.properties", classOf[ProfileServiceImpl]).foreach { url =>
      profiles ++= buildProfiles(url, false)
    }
    profiles.toList
  }

  /**加载META-INF/convention-default.properties*/
  private def loadDefaultProfile(): Profile = {
    val convention_default = ClassLoaders.getResource("META-INF/beangle/convention-default.properties", classOf[ProfileServiceImpl])
    if (null == convention_default) { throw new RuntimeException("cannot find convention-default.properties!") }
    buildProfiles(convention_default, true)(0)
  }

  private def buildProfiles(url: URL, isDefault: Boolean): Seq[Profile] = {
    val myProfiles = new collection.mutable.ListBuffer[Profile]
    val props = IOs.readJavaProperties(url)
    if (isDefault) {
      val profile = populatProfile(props, "default")
      myProfiles += profile
    } else {
      var profileIndex: Int = 0
      var ifBreak = true
      while (ifBreak) {
        var profile = populatProfile(props, "profile" + profileIndex)
        if (null == profile) {
          ifBreak = false
        } else {
          myProfiles += profile
        }
        profileIndex += 1
      }
    }
    myProfiles
  }

  private def populatProfile(props: Map[String, String], name: String): Profile = {
    val actionPattern = props.get(name + ".actionPattern").orNull
    if (Strings.isEmpty(actionPattern)) null
    else {
      val profile = new Profile(name, actionPattern)
      populateAttr(profile, "actionSuffix", props)
      populateAttr(profile, "viewPath", props)
      populateAttr(profile, "viewExtension", props)
      populateAttr(profile, "viewPathStyle", props)
      populateAttr(profile, "defaultMethod", props)
      populateAttr(profile, "uriPath", props)
      populateAttr(profile, "uriPathStyle", props)
      populateAttr(profile, "uriExtension", props)
      populateAttr(profile, "actionScan", props)
      profile
    }
  }

  private def populateAttr(profile: Profile, attr: String, props: Map[String, String]) {
    var value = props.get(profile.name + "." + attr).orNull
    try {
      if (null == value) value = PropertyUtils.getProperty(defaultProfile, attr)
      PropertyUtils.copyProperty(profile, attr, value)
    } catch {
      case e: Exception => error(s"error attr ${attr} for profile")
    }
  }
}

class ProfileServiceImpl extends ProfileService with Logging {

  val profiles: List[Profile] = ProfileServiceImpl.loadProfiles

  // 匹配缓存[String,Profile]
  private val cache = new ju.concurrent.ConcurrentHashMap[String, Profile]

  def getProfile(className: String): Profile = {
    var matched = cache.get(className)
    if (null != matched) { return matched }
    var index: Int = -1
    var patternLen: Int = 0
    for (profile <- profiles if (profile.isMatch(className))) {
      var newIndex = profile.matchedIndex(className)
      if (newIndex >= index && profile.actionPattern.length >= patternLen) {
        matched = profile
        index = newIndex
        patternLen = profile.actionPattern.length
      }
    }
    if (matched == null) {
      matched = ProfileServiceImpl.defaultProfile
    }
    cache.put(className, matched)
    debug(s"${className} match profile:${matched}")
    matched
  }

  def getProfile(clazz: Class[_]): Profile = {
    getProfile(clazz.getName())
  }

}