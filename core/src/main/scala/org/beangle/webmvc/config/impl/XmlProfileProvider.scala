package org.beangle.webmvc.config.impl

import java.net.URL

import scala.xml.{ Node, XML }

import org.beangle.commons.bean.PropertyUtils.{ copyProperty, getProperty }
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.config.{ ProfileConfig, ProfileProvider }

@description("基于xml的配置提供者")
class XmlProfileProvider extends ProfileProvider {

  private val defaultProfile = loadDefaultProfile()

  /**
   * 初始化配置META-INF/beangle/mvc-config.xml
   */
  def loadProfiles(): List[ProfileConfig] = {
    val profiles = new collection.mutable.ListBuffer[ProfileConfig]
    ClassLoaders.getResources("META-INF/beangle/mvc-config.xml").foreach { url =>
      profiles ++= readXmlToProfiles(url)
    }
    profiles.toList
  }

  /**加载META-INF/beangle/mvc-default.xml*/
  private def loadDefaultProfile(): ProfileConfig = {
    val convention_default = ClassLoaders.getResource("META-INF/beangle/mvc-default.xml")
    if (null == convention_default) throw new RuntimeException("cannot find mvc-default.xml!")
    readXmlToProfiles(convention_default)(0)
  }

  private def readXmlToProfiles(url: URL): Seq[ProfileConfig] = {
    val profiles = new collection.mutable.ListBuffer[ProfileConfig]
    XML.load(url) \ "profile" foreach { profileElem =>
      val name = (profileElem \ "@name").text
      val pattern = (profileElem \ "@pattern").text
      val profile = new ProfileConfig(name, pattern)
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

      val urlNodes = profileElem \ "url"
      if (urlNodes.isEmpty) {
        copyDefaultProperties(profile, "urlPath", "urlStyle", "urlSuffix")
      } else {
        urlNodes foreach { elem =>
          readProperty(elem, profile, "path", "urlPath")
          if (!profile.urlPath.endsWith("/")) profile.urlPath += "/"
          readProperty(elem, profile, "style", "urlStyle")
          readProperty(elem, profile, "suffix", "urlSuffix")
        }
      }

      val interceptorNodes = profileElem \\ "interceptor"
      if (interceptorNodes.isEmpty) {
        copyDefaultProperties(profile, "interceptorNames")
      } else {
        val interceptors = new collection.mutable.ListBuffer[String]
        interceptorNodes foreach { elem =>
          interceptors += (elem \ "@name").text
        }
        profile.interceptorNames = interceptors.toArray
      }
      profile.source = url
      profiles += profile
    }
    profiles
  }

  private def readProperty(elem: Node, profile: ProfileConfig, attrName: String, propertyName: String): Unit = {
    val xmlAttribute = "@" + attrName
    if (!(elem \ xmlAttribute).isEmpty) {
      copyProperty(profile, propertyName, (elem \ xmlAttribute).text.intern)
    } else {
      copyProperty(profile, propertyName, getProperty(defaultProfile, propertyName))
    }
  }

  private def copyDefaultProperties(profile: ProfileConfig, properties: String*): Unit = {
    properties foreach { propertyName =>
      copyProperty(profile, propertyName, getProperty(defaultProfile, propertyName))
    }
  }
}