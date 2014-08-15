package org.beangle.webmvc.route.impl

import java.lang.reflect.Method
import java.net.URL
import java.{ util => ju }
import org.beangle.commons.bean.PropertyUtils.{ copyProperty, getProperty }
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.route.{ Action, ActionMapping, Profile, RouteService }
import org.beangle.webmvc.route.StrutsAction

object RouteServiceImpl extends Logging {

  val defaultProfile = loadDefaultProfile()

  /**
   * 初始化配置META-INF/convention-route.properties
   */
  def loadProfiles(): List[Profile] = {
    val profiles = new collection.mutable.ListBuffer[Profile]
    ClassLoaders.getResources("META-INF/beangle/convention-route.properties").foreach { url =>
      profiles ++= buildProfiles(url, false)
    }
    profiles.toList
  }

  /**加载META-INF/convention-default.properties*/
  private def loadDefaultProfile(): Profile = {
    val convention_default = ClassLoaders.getResource("META-INF/beangle/convention-default.properties")
    if (null == convention_default) throw new RuntimeException("cannot find convention-default.properties!")
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
      populateAttr(profile, "viewSuffix", props)
      populateAttr(profile, "viewPathStyle", props)
      populateAttr(profile, "defaultMethod", props)
      populateAttr(profile, "uriPath", props)
      populateAttr(profile, "uriPathStyle", props)
      populateAttr(profile, "uriSuffix", props)
      populateAttr(profile, "actionScan", props)
      profile
    }
  }

  private def populateAttr(profile: Profile, attr: String, props: Map[String, String]) {
    props.get(profile.name + "." + attr) match {
      case Some(v) => if (Strings.isBlank(v)) copyProperty(profile, attr, null) else copyProperty(profile, attr, v)
      case None => copyProperty(profile, attr, getProperty(defaultProfile, attr))
    }
  }
}

class RouteServiceImpl extends RouteService with Logging {

  private val classProfiles = new collection.mutable.HashMap[String, Profile]

  val viewMapper = new DefaultViewMapper

  val actionMappingBuilder = new DefaultActionMappingBuilder(this)

  val profiles: List[Profile] = RouteServiceImpl.loadProfiles

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

  /**
   * 根据class对应的profile获得ctl/action类中除去后缀后的名字。<br>
   * 如果对应profile中是uriStyle,那么类中只保留简单类名，去掉后缀，并且小写第一个字母。<br>
   * 否则加上包名，其中的.编成URI路径分割符。包名不做其他处理。<br>
   * 复杂URL,以/开始
   */
  def buildAction(clazz: Class[_]): StrutsAction = {
    val profile = getProfile(clazz.getName)
    val url = ActionURIBuilder.build(clazz, profile)
    val lastSlash = url.lastIndexOf('/')
    val result = Tuple2(url.substring(0, lastSlash), url.substring(lastSlash + 1))
    new StrutsAction(result._1, result._2, profile.defaultMethod)
  }

  def buildMappings(clazz: Class[_]): Seq[Tuple2[ActionMapping, Method]] = {
    actionMappingBuilder.build(clazz)
  }
  /**
   * viewname -> 页面路径的映射
   */
  def mapView(className: String, viewName: String): String = {
    val profile = getProfile(className)
    if (null == profile) throw new RuntimeException(s"no convention profile for $className")
    viewMapper.map(className, viewName, profile)
  }
}
