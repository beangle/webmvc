package org.beangle.webmvc.route.impl

import java.lang.reflect.Method
import java.net.URL

import scala.xml.{ Node, XML }

import org.beangle.commons.bean.PropertyUtils.{ copyProperty, getProperty }
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.route.{ ActionMapping, Profile, RouteService, StrutsAction }

object RouteServiceImpl extends Logging {

  val defaultProfile = loadDefaultProfile()

  /**
   * 初始化配置META-INF/convention-route.xml
   */
  def loadProfiles(): List[Profile] = {
    val profiles = new collection.mutable.ListBuffer[Profile]
    ClassLoaders.getResources("META-INF/beangle/convention-route.xml").foreach { url =>
      profiles ++= readXmlToProfiles(url)
    }
    profiles.toList
  }

  /**加载META-INF/convention-default.xml*/
  private def loadDefaultProfile(): Profile = {
    val convention_default = ClassLoaders.getResource("META-INF/beangle/convention-default.xml")
    if (null == convention_default) throw new RuntimeException("cannot find convention-default.xml!")
    readXmlToProfiles(convention_default)(0)
  }

  private def readXmlToProfiles(url: URL): Seq[Profile] = {
    val profiles = new collection.mutable.ListBuffer[Profile]
    XML.load(url) \ "profile" foreach { profileElem =>
      val name = (profileElem \ "@name").text
      val pattern = (profileElem \ "@pattern").text
      val profile = new Profile(name, pattern)
      val actionNodes = profileElem \ "action"
      if (actionNodes.isEmpty) {
        copyDefaultProperties(profile, "actionSuffix", "defaultMethod")
      } else {
        actionNodes foreach { elem =>
          readProperty(elem, profile, "suffix", "actionSuffix")
          readProperty(elem, profile, "defaultMethod", "defaultMethod")
        }
      }

      val viewNodes = profileElem \ "view"
      if (viewNodes.isEmpty) {
        copyDefaultProperties(profile, "viewPath", "viewPathStyle", "viewSuffix")
      } else {
        viewNodes foreach { elem =>
          readProperty(elem, profile, "path", "viewPath")
          readProperty(elem, profile, "style", "viewPathStyle")
          readProperty(elem, profile, "suffix", "viewSuffix")
        }
      }

      val uriNodes = profileElem \ "uri"
      if (uriNodes.isEmpty) {
        copyDefaultProperties(profile, "uriPath", "uriPathStyle", "uriSuffix")
      } else {
        uriNodes foreach { elem =>
          readProperty(elem, profile, "path", "uriPath")
          readProperty(elem, profile, "style", "uriPathStyle")
          readProperty(elem, profile, "suffix", "uriSuffix")
        }
      }
      profiles += profile
    }
    profiles
  }

  private def readProperty(elem: Node, profile: Profile, attrName: String, propertyName: String): Unit = {
    val xmlAttribute = "@" + attrName
    if (!(elem \ xmlAttribute).isEmpty) {
      copyProperty(profile, propertyName, (elem \ xmlAttribute).text)
    } else {
      copyProperty(profile, propertyName, getProperty(defaultProfile, propertyName))
    }
  }

  private def copyDefaultProperties(profile: Profile, properties: String*): Unit = {
    properties foreach { propertyName =>
      copyProperty(profile, propertyName, getProperty(defaultProfile, propertyName))
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
