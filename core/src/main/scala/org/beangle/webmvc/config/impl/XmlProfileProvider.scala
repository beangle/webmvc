/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.config.impl

import java.net.URL

import org.beangle.commons.bean.Properties.{copy, get}
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.config.{ProfileConfig, ProfileProvider}

import scala.xml.{Node, XML}

@description("基于xml的配置提供者")
class XmlProfileProvider extends ProfileProvider {

  private val defaultProfile = loadDefaultProfile()

  /**
   * 初始化配置META-INF/beangle/mvc.xml
   */
  def loadProfiles(): List[ProfileConfig] = {
    val profiles = new collection.mutable.ListBuffer[ProfileConfig]
    ClassLoaders.getResources("META-INF/beangle/mvc.xml").foreach { url =>
      profiles ++= readXmlToProfiles(url)
    }
    profiles.toList
  }

  private def loadDefaultProfile(): ProfileConfig = {
    val pc = new ProfileConfig("default", "*")
    pc.actionSuffix = "Action"
    pc.defaultMethod = "index"
    pc.viewPath = "/"
    pc.viewPathStyle = "full"
    pc.viewType = "freemarker"
    pc.viewSuffix = ".ftl"
    pc.urlPath = "/"
    pc.urlStyle = "seo"
    pc
  }

  private def readXmlToProfiles(url: URL): Seq[ProfileConfig] = {
    val profiles = new collection.mutable.ListBuffer[ProfileConfig]
    XML.load(url) \ "profile" foreach { profileElem =>
      val name = (profileElem \ "@name").text
      val profile = new ProfileConfig(name, (profileElem \ "@package").text)
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
        interceptorNodes foreach (elem => interceptors += (elem \ "@name").text)
        profile.interceptorNames = interceptors.toArray
      }

      val decoratorNodes = profileElem \\ "decorator"
      if (decoratorNodes.nonEmpty) {
        val decorators = new collection.mutable.ListBuffer[String]
        decoratorNodes foreach (elem => decorators += (elem \ "@name").text)
        profile.decoratorNames = decorators.toArray
      }
      profile.source = url
      profiles += profile
    }
    profiles.toSeq
  }

  private def readProperty(elem: Node, profile: ProfileConfig, attrName: String, propertyName: String): Unit = {
    val xmlAttribute = "@" + attrName
    if ((elem \ xmlAttribute).nonEmpty) {
      copy(profile, propertyName, (elem \ xmlAttribute).text.intern)
    } else {
      copy(profile, propertyName, get(defaultProfile, propertyName))
    }
  }

  private def copyDefaultProperties(profile: ProfileConfig, properties: String*): Unit = {
    properties foreach { propertyName =>
      copy(profile, propertyName, get(defaultProfile, propertyName))
    }
  }
}
