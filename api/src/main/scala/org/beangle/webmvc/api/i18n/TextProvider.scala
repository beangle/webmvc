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
package org.beangle.webmvc.api.i18n

/**
 * @author chaostone
 */
trait TextProvider {
  /**
   * Gets a message based on a message key, or null if no message is found.
   */
  def apply(key: String): Option[String]

  /**
   * Gets a message based on a key using the supplied obj, as defined in
   * java.text.MessageFormat, or, if the message is not found, a
   * supplied default value is returned.
   */
  def apply(key: String, defaultValue: String, obj: Any*): String
}
