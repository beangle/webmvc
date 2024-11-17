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

package org.beangle.webmvc.config

import org.beangle.commons.lang.Strings.split

object Path {

  def isTailMatch(path: String): Boolean = {
    path.charAt(path.length - 1) == '*'
  }

  def isTailPattern(path: String): Boolean = {
    path.endsWith("*}")
  }

  def isPattern(pathSegment: String): Boolean = {
    pathSegment.charAt(0) == '{' && pathSegment.charAt(pathSegment.length - 1) == '}'
  }

  /**
   * /a/b/c => ()
   * /{a}/&star/{c} => (a->0,1->1,c->2)
   * /a/b/{c}/{a*} => (c->2,a*->3)
   */
  def parse(pattern: String): Map[String, Integer] = {
    val parts = split(pattern, "/")
    val params = new collection.mutable.HashMap[String, Integer]
    var i = 0
    while (i < parts.length) {
      val p = parts(i)
      if (p.charAt(0) == '{' && p.charAt(p.length - 1) == '}') {
        params.put(p.substring(1, p.length - 1), Integer.valueOf(i))
      } else if (p == "*") {
        params.put(String.valueOf(i), Integer.valueOf(i))
      }
      i += 1
    }
    params.toMap
  }
}
