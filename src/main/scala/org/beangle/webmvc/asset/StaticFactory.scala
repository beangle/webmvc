/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.asset

import jakarta.servlet.ServletContext
import org.beangle.commons.bean.Factory
import org.beangle.commons.collection.Collections
import org.beangle.commons.config.XmlConfigs
import org.beangle.commons.lang.Strings
import org.beangle.commons.xml.Node
import org.beangle.webmvc.asset.Static.{Module, Resource}

class StaticFactory extends Factory[Static] {
  private val defaultConfigLocation = "classpath*:beangle.xml"

  var configs: XmlConfigs = _

  var base: String = _

  var sc: ServletContext = _

  override def getObject: Static = {
    if (Strings.isBlank(base) || base == "-") {
      base = if sc.getContextPath == "/" then "/static" else sc.getContextPath + "/static"
    }
    val rs = new Static(base)
    (configs.load(defaultConfigLocation) \ "mvc") foreach { mvc =>
      rs.addResources(buildResource(mvc))
    }
    rs
  }

  private def buildResource(mvc: Node): List[Resource] = {
    val rss = Collections.newBuffer[Resource]
    (mvc \ "static" \ "bundle") foreach { e =>
      var version = (e \ "@version").text
      if (version.startsWith("${") && version.endsWith("}")) {
        val v2 = System.getProperty(Strings.substringBetween(version, "${", "}"))
        if (v2 == null) {
          throw new RuntimeException(s"Cannot find system property ${version}")
        }
        version = v2
      }
      val bundle = new Resource((e \ "@name").text, version)
      val modules = Collections.newBuffer[Module]
      e \ "module" foreach { m =>

        var js: Option[String] = None
        (m \ "@js") foreach { jsele =>
          js = Some(jsele.text)
        }
        val css = (m \ "@css").text
        val depends = (m \ "@depends").text
        modules += Module(bundle, (m \ "@name").text, js, Strings.split(css), Strings.split(depends))
      }
      bundle.modules = modules.toList
      rss += bundle
    }
    rss.toList
  }
}
