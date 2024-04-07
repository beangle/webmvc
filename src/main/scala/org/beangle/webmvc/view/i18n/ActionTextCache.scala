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

package org.beangle.webmvc.view.i18n

class ActionTextCache {
  private var caches: Map[Class[_], Map[String, String]] = Map.empty

  def getText(clazz: Class[_], key: String): Option[String] = {
    caches.get(clazz) match
      case None => None
      case Some(kvs) => kvs.get(key)
  }

  def update(clazz: Class[_], key: String, value: String, common: Boolean): Unit = {
    caches.get(clazz) match
      case None =>
        if common then caches += (clazz, Map(key.intern() -> value.intern()))
        else caches += (clazz, Map(key -> value))
      case Some(map) =>
        if common then caches += (clazz, map + (key.intern() -> value.intern()))
        else caches += (clazz, map + (key -> value))

  }
}
