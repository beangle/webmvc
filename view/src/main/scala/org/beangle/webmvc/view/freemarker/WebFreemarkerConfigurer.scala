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
package org.beangle.webmvc.view.freemarker

import java.io.{ File, IOException }

import org.beangle.commons.lang.Strings.{ split, substringAfter }
import org.beangle.commons.web.context.ServletContextHolder
import org.beangle.template.freemarker.{ BeangleClassTemplateLoader, Configurer }

import freemarker.cache.{ FileTemplateLoader, MultiTemplateLoader, TemplateLoader, WebappTemplateLoader }
import freemarker.template.ObjectWrapper

/**
 * @author chaostone
 */
class WebFreemarkerConfigurer extends Configurer {
  override def createTemplateLoader(props: Map[String, String]): TemplateLoader = {
    templatePath = ServletContextHolder.context.getInitParameter("templatePath")
    val paths: Array[String] = split(templatePath, ",")
    val loaders = new collection.mutable.ListBuffer[TemplateLoader]
    for (path <- paths) {
      if (path.startsWith("class://")) {
        loaders += new BeangleClassTemplateLoader(substringAfter(path, "class://"))
      } else if (path.startsWith("file://")) {
        try {
          loaders += new FileTemplateLoader(new File(substringAfter(path, "file://")))
        } catch {
          case e: IOException =>
            throw new RuntimeException("templatePath: " + path + " cannot be accessed", e)
        }
      } else if (path.startsWith("webapp://")) {
        loaders += new WebappTemplateLoader(ServletContextHolder.context, substringAfter(path, "webapp://"))
      } else {
        throw new RuntimeException("templatePath: " + path
          + " is not well-formed. Use [class://|file://|webapp://] seperated with ,")
      }
    }
    new MultiTemplateLoader(loaders.toArray[TemplateLoader])
  }

  override def createObjectWrapper(props: Map[String, String]): ObjectWrapper = {
    val wrapper = new CachedObjectWrapper()
    wrapper.setUseCache(false)
    wrapper
  }
}
