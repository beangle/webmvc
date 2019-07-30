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
package org.beangle.webmvc.view.impl

import org.beangle.cdi.{Container, ContainerListener}
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.view.{TagLibrary, TagLibraryProvider}

@description("所有标签库提供者")
class ContainerTaglibraryProvider extends TagLibraryProvider with ContainerListener {

  var tagLibraries: Map[String, TagLibrary] = Map.empty

  override def onStarted(container: Container): Unit = {
    tagLibraries = container.getBeans(classOf[TagLibrary]).map {
      case (key, library) =>
        val name = key.toString
        if (name.contains(".")) (Strings.substringAfterLast(name, "."), library) else (name, library)
    }
  }
}
