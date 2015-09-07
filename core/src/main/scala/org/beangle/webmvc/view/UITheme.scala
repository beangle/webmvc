/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.webmvc.view

/**
 * UITheme represent css or images resource bundle name.
 *
 * @author chaostone
 * @since 3.0.0
 */
class UITheme(val base: String) {

  def iconurl(themeName: String,name: String): String = {
    iconurl(themeName,name, "16x16")
  }

  def iconurl(themeName: String,name: String, size: Integer): String = {
    val sb = new StringBuilder()
    sb.append(size).append('x').append(size)
    iconurl(themeName,name, sb.toString())
  }

  def iconurl(themeName: String,name: String, size: String): String = {
    val sb = new StringBuilder(80)
    sb.append(base)
    sb.append(themeName).append("/icons/").append(size)
    if (!name.startsWith("/")) sb.append('/')
    sb.append(name)
    sb.toString()
  }

  def cssurl(themeName: String,name: String): String = {
    val sb = new StringBuilder(80)
    sb.append(themeName)
    if (!name.startsWith("/")) sb.append('/')
    sb.append(name)
    sb.toString()
  }


}