package org.beangle.webmvc.config.impl

import org.beangle.commons.lang.Strings.{ substringBeforeLast, unCamel, uncapitalize, capitalize }
import org.beangle.webmvc.api.annotation.action
import org.beangle.webmvc.config.Profile
import org.beangle.commons.text.inflector.en.EnNounPluralizer
import org.beangle.webmvc.api.action.EntitySupport

object ActionNameBuilder {

  val pluralizer = new EnNounPluralizer
  /**
   * Return namespace and action name.
   * 
   *  <li>action name start with /
   *  <li>namespace start with / and DONOT ends with /
   *
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
          if (classOf[EntitySupport[_]].isAssignableFrom(clazz)) {
            val matchedName = profile.getMatched(className)
            val lastSlash = matchedName.lastIndexOf('/')
            if (-1 == lastSlash) {
              nameBuilder.append(unCamel(pluralizer.pluralize(matchedName)))
            } else {
              nameBuilder.append(unCamel(matchedName.substring(0, lastSlash + 1)))
              nameBuilder.append(unCamel(pluralizer.pluralize(matchedName.substring(lastSlash + 1))))
            }
          } else {
            nameBuilder.append(unCamel(profile.getMatched(className)))
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
    if (namespace.length > 0 && namespace.charAt(namespace.length - 1) == '/') namespace.deleteCharAt(namespace.length - 1)
    (nameBuilder.toString, namespace.toString)
  }
}