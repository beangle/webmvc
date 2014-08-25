package org.beangle.webmvc.config.impl

import java.net.URL
import scala.xml.{ Node, XML }
import org.beangle.commons.bean.PropertyUtils.{ copyProperty, getProperty }
import org.beangle.commons.lang.ClassLoaders
import org.beangle.webmvc.config.{ Profile, ProfileProvider }
import org.beangle.webmvc.execution.Interceptor
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.lang.annotation.description

class ProfileBuilder(val name: String, val actionPattern: String) {

  // action类名后缀
  var actionSuffix: String = _

  // 缺省的action中的方法
  var defaultMethod = "index"

  // 路径前缀
  var viewPath: String = _

  // 路径模式
  var viewPathStyle = "simple"

  // 路径后缀
  var viewSuffix: String = _

  // View Type (freemarker chain)
  var viewType: String = _

  //end with /
  var uriPath = "/"

  // URI style
  var uriStyle = "simple"

  /** URI的后缀 */
  var uriSuffix: String = _

  var interceptors: Array[Interceptor] = Array()

  var source: URL = _
  def mkProfile(): Profile = {
    new Profile(name, actionPattern, actionSuffix, defaultMethod, viewPath, viewPathStyle, viewSuffix, viewType, uriPath, uriStyle, uriSuffix, interceptors, source)
  }
}

@description("基于xml的配置提供者")
class XmlProfileProvider extends ProfileProvider {

  private val defaultProfile = loadDefaultProfile()

  /**
   * 初始化配置META-INF/beangle/mvc-config.xml
   */
  def loadProfiles(): List[Profile] = {
    val profiles = new collection.mutable.ListBuffer[Profile]
    ClassLoaders.getResources("META-INF/beangle/mvc-config.xml").foreach { url =>
      profiles ++= readXmlToProfiles(url)
    }
    profiles.sorted.toList
  }

  /**加载META-INF/beangle/mvc-default.xml*/
  private def loadDefaultProfile(): Profile = {
    val convention_default = ClassLoaders.getResource("META-INF/beangle/mvc-default.xml")
    if (null == convention_default) throw new RuntimeException("cannot find mvc-default.xml!")
    readXmlToProfiles(convention_default)(0)
  }

  private def readXmlToProfiles(url: URL): Seq[Profile] = {
    val profiles = new collection.mutable.ListBuffer[Profile]
    XML.load(url) \ "profile" foreach { profileElem =>
      val name = (profileElem \ "@name").text
      val pattern = (profileElem \ "@pattern").text
      val profile = new ProfileBuilder(name, pattern)
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
        copyDefaultProperties(profile, "viewPath", "viewPathStyle", "viewType", "viewSuffix")
      } else {
        viewNodes foreach { elem =>
          readProperty(elem, profile, "path", "viewPath")
          readProperty(elem, profile, "style", "viewPathStyle")
          readProperty(elem, profile, "suffix", "viewSuffix")
          readProperty(elem, profile, "type", "viewType")
        }
      }

      val uriNodes = profileElem \ "uri"
      if (uriNodes.isEmpty) {
        copyDefaultProperties(profile, "uriPath", "uriStyle", "uriSuffix")
      } else {
        uriNodes foreach { elem =>
          readProperty(elem, profile, "path", "uriPath")
          if (!profile.uriPath.endsWith("/")) profile.uriPath += "/"
          readProperty(elem, profile, "style", "uriStyle")
          readProperty(elem, profile, "suffix", "uriSuffix")
        }
      }

      val interceptorNodes = profileElem \\ "interceptor"
      if (interceptorNodes.isEmpty) {
        copyDefaultProperties(profile, "interceptors")
      } else {
        val interceptors = new collection.mutable.ListBuffer[Interceptor]
        interceptorNodes foreach { elem =>
          interceptors += Reflections.newInstance(ClassLoaders.loadClass((elem \ "@class").text)).asInstanceOf[Interceptor]
        }
        profile.interceptors = interceptors.toArray
      }
      profile.source = url
      profiles += profile.mkProfile
    }
    profiles
  }

  private def readProperty(elem: Node, profile: ProfileBuilder, attrName: String, propertyName: String): Unit = {
    val xmlAttribute = "@" + attrName
    if (!(elem \ xmlAttribute).isEmpty) {
      copyProperty(profile, propertyName, (elem \ xmlAttribute).text.intern)
    } else {
      copyProperty(profile, propertyName, getProperty(defaultProfile, propertyName))
    }
  }

  private def copyDefaultProperties(profile: ProfileBuilder, properties: String*): Unit = {
    properties foreach { propertyName =>
      copyProperty(profile, propertyName, getProperty(defaultProfile, propertyName))
    }
  }
}