/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.struts2.config.helper

import scala.collection.JavaConversions.asScalaSet

import org.beangle.commons.io.{ IOs, ResourcePatternResolver }

import com.opensymphony.xwork2.ObjectFactory
import com.opensymphony.xwork2.config.Configuration
import com.opensymphony.xwork2.config.entities.ActionConfig
import com.opensymphony.xwork2.inject.{ Container, Inject }
/**
 * @author chaostone
 */
class S2ConfigurationHelper {

  @Inject
  var configuration: Configuration = _
  @Inject
  var container: Container = _
  @Inject
  var objectFactory: ObjectFactory = _

  def getNamespaces(): collection.Set[String] = {
    val allActionConfigs = configuration.getRuntimeConfiguration().getActionConfigs()
    if (allActionConfigs != null) allActionConfigs.keySet else Set.empty
  }

  def getActionNames(namespace: String): collection.Set[String] = {
    val allActionConfigs = configuration.getRuntimeConfiguration()
      .getActionConfigs()
    var actionNames: collection.Set[String] = Set.empty[String]
    if (allActionConfigs != null) {
      val actionMappings = allActionConfigs.get(namespace)
      if (actionMappings != null) actionNames = actionMappings.keySet
    }
    actionNames
  }

  def getActionConfig(namespace: String, actionName: String): ActionConfig = {
    var config: ActionConfig = null
    val allActionConfigs = configuration.getRuntimeConfiguration.getActionConfigs()
    if (allActionConfigs != null) {
      val actionMappings = allActionConfigs.get(namespace)
      if (actionMappings != null) config = actionMappings.get(actionName)
    }
    config
  }

  def getJarProperties(): List[Map[String, String]] = {
    val resolver = new ResourcePatternResolver
    val urls = resolver.getResources("classpath*:META-INF/maven/**/pom.properties")
    val poms = new collection.mutable.ListBuffer[Map[String, String]]
    urls foreach { url =>
      poms += IOs.readJavaProperties(url)
    }
    poms.toList
  }

}
