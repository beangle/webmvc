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

package org.beangle.webmvc.view

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{ClassLoaders, Strings}

import java.net.URL

object Static {

  class Resource(val name: String, val version: String) {
    var modules: Seq[Module] = _
  }

  case class Module(bundle: Resource, name: String, js: Option[String], css: Array[String], depends: Array[String]) {
    override def toString: String = {
      val sb = new StringBuilder("{")
      js foreach { j =>
        sb.append(s"""js:"${bundle.name}/${bundle.version}/$j"""")
      }
      if (css.length > 0) {
        if (sb.length > 1) {
          sb.append(",")
        }
        sb.append("css:")
        sb.append(css.map(x => s""""${bundle.name}/${bundle.version}/$x"""").mkString("[", ",", "]"))
      }
      if (depends.length > 0) {
        if (sb.length > 1) {
          sb.append(",")
        }
        sb.append("deps:")
        sb.append(depends.map(x => s""""$x"""").mkString("[", ",", "]"))
      }
      sb.append("}")
      sb.mkString
    }
  }

  val Default: Static = buildDefault()

  def buildDefault(): Static = {
    val rs = new Static
    ClassLoaders.getResources("META-INF/beangle/mvc-default.xml") foreach { url =>
      rs.addResources(buildResource(url))
    }
    ClassLoaders.getResources("META-INF/beangle/mvc.xml") foreach { url =>
      rs.addResources(buildResource(url))
    }
    rs
  }

  private def buildResource(url: URL): List[Resource] = {
    val xml = scala.xml.XML.load(url.openStream())
    val rss = Collections.newBuffer[Resource]
    (xml \\ "static" \\ "bundle") foreach { e =>
      val bundle = new Resource((e \ "@name").text, (e \ "@version").text)
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

class Static {

  import Static.Resource

  private val registry = Collections.newMap[String, Resource]

  var base: String = _

  var modules: Map[String, Static.Module] = Map.empty

  def resources: Iterable[Resource] = {
    registry.values
  }

  def addResources(res: List[Resource]): this.type = {
    res.foreach { r =>
      registry.get(r.name) match {
        case None => register(r)
        case Some(er) =>
          if (r.version.compareTo(er.version) > 0) {
            register(r)
          }
      }
    }
    this
  }

  private def register(r: Resource): Unit = {
    registry.put(r.name, r)
    r.modules foreach { m =>
      this.modules += (m.name -> m)
    }
  }

  def path(bundle: String, file: String): String = {
    val fileName = {
      if (Strings.isEmpty(file)) {
        ""
      } else {
        if (file.charAt(0) == '/') file else "/" + file
      }
    }
    registry.get(bundle) match {
      case Some(r) => s"$bundle/${r.version}$fileName"
      case None => s"$bundle/undefined$fileName"
    }
  }

  def url(bundle: String, file: String): String = {
    s"$base/${path(bundle, file)}"
  }

  def script(bundle: String, fileName: String, deferable: Boolean): String = {
    val l = url(bundle, fileName)
    val defer = if (deferable) "defer" else ""
    s"""<script type="text/javascript" crossorigin="anonymous"  $defer src="$l"></script>"""
  }

  def load(moduleNames: java.util.List[String]): String = {
    val modules = Static.Default.modules
    val csses = Collections.newBuffer[String]
    val scripts = Collections.newBuffer[String]
    scala.jdk.javaapi.CollectionConverters.asScala(moduleNames).foreach { moduleName =>
      modules.get(moduleName) foreach { m =>
        m.css foreach { css =>
          csses += this.css(m.bundle.name, css)
        }
        m.js foreach { js =>
          scripts += script(m.bundle.name, js,false)
        }
      }
    }
    (csses ++ scripts).mkString("\n")
  }

  def css(bundle: String, fileName: String): String = {
    val l = url(bundle, fileName)
    s"""<link rel="stylesheet" crossorigin="anonymous" href="$l"/>"""
  }

  def module_contents: collection.Map[String, String] = {
    val contents = Collections.newMap[String, String]
    val modules = Static.Default.modules
    modules foreach { case (n, m) =>
      contents += n -> m.toString
    }
    contents
  }
}
