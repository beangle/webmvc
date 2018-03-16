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
package org.beangle.webmvc.config.impl

import org.beangle.commons.lang.Strings.{ substringBeforeLast, unCamel, uncapitalize, capitalize }
import org.beangle.webmvc.api.annotation.action
import org.beangle.webmvc.config.Profile
import org.beangle.commons.text.inflector.en.EnNounPluralizer
import org.beangle.webmvc.api.action.EntitySupport

object ActionNameBuilder {

  /**
   * Return namespace and action name.
   *
   *  <li>namespace start with / and DONOT ends with /(except only /)
   *  <li>action name contains namespace and DONOT ends with /(except only /)
   */
  def build(clazz: Class[_], profile: Profile): Tuple2[String, String] = {
    val className = clazz.getName
    val ann = clazz.getAnnotation(classOf[action])
    val nameBuilder = new StringBuilder()
    val namespace = new StringBuilder()
    nameBuilder.append(profile.urlPath)

    if (null == ann) {
      profile.urlStyle match {
        case Profile.SHORT_URI =>
          val simpleName = className.substring(className.lastIndexOf('.') + 1)
          nameBuilder.append(uncapitalize(simpleName.substring(0, simpleName.length - profile.actionSuffix.length)))
        case Profile.SIMPLE_URI =>
          nameBuilder.append(profile.getMatched(className))
        case Profile.SEO_URI =>
          nameBuilder.append(unCamel(profile.getMatched(className)))
        case Profile.PLUR_SEO_URI =>
          val matchedName = profile.getMatched(className)
          val lastSlash = matchedName.lastIndexOf('/')
          if (-1 == lastSlash) {
            nameBuilder.append(unCamel(EnNounPluralizer.pluralize(matchedName)))
          } else {
            nameBuilder.append(unCamel(matchedName.substring(0, lastSlash + 1)))
            nameBuilder.append(unCamel(EnNounPluralizer.pluralize(matchedName.substring(lastSlash + 1))))
          }
        case _ =>
          throw new RuntimeException("unsupported uri style " + profile.urlStyle)
      }
      namespace ++= nameBuilder.substring(0, nameBuilder.lastIndexOf('/'))
    } else {
      val name = ann.value()
      namespace ++= nameBuilder
      if (!name.startsWith("/")) {
        if (Profile.SEO_URI == profile.urlStyle || Profile.PLUR_SEO_URI == profile.urlStyle) {
          val matched = profile.getMatched(className)
          val lastSlashIdx = matched.lastIndexOf('/')
          if (lastSlashIdx > 0) {
            val middleName = unCamel(matched.substring(0, lastSlashIdx))
            namespace ++= middleName
            nameBuilder ++= middleName
          }
          if (name.length > 0) {
            if (lastSlashIdx > 0) nameBuilder.append('/').append(name) else nameBuilder.append(name)
          }
        } else {
          nameBuilder.append(name)
        }
      } else {
        nameBuilder.append(name.substring(1))
      }
    }
    (deleteTailSlash(nameBuilder), deleteTailSlash(namespace))
  }

  def deleteTailSlash(name: StringBuilder): String = {
    val length = name.length
    if (length > 1 && name.charAt(length - 1) == '/') name.deleteCharAt(length - 1)
    name.toString
  }
}
