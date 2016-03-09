/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.view.tag.freemarker

import java.io.Reader
import org.beangle.webmvc.view.tag.Themes
import freemarker.cache.TemplateLoader

class HierarchicalTemplateLoader(loader: TemplateLoader) extends TemplateLoader {

  def findTemplateSource(name: String): Object = {
    var resource = name
    var source = loader.findTemplateSource(resource)
    if (null == source) {
      val searched = new collection.mutable.HashSet[String]
      searched.add(resource)
      var break = false
      do {
        resource = Themes.getParentTemplate(resource)
        if (null == resource || searched.contains(resource)) break = true
        else {
          source = loader.findTemplateSource(resource)
          searched.add(resource)
        }
      } while (null == source && !break)
    }
    return source
  }

  def getLastModified(templateSource: Object): Long = {
    return loader.getLastModified(templateSource)
  }

  def getReader(templateSource: Object, encoding: String): Reader = {
    return loader.getReader(templateSource, encoding)
  }

  def closeTemplateSource(templateSource: Object) {
    loader.closeTemplateSource(templateSource)
  }
}