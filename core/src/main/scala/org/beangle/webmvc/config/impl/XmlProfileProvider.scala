package org.beangle.webmvc.config.impl

import java.net.URL
import scala.xml.{ Node, XML }
import org.beangle.commons.bean.PropertyUtils.{ copyProperty, getProperty }
import org.beangle.commons.lang.ClassLoaders
import org.beangle.webmvc.config.{ Profile, ProfileProvider }
import org.beangle.webmvc.execution.Interceptor
import org.beangle.commons.lang.reflect.Reflections

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
    profiles.toList
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