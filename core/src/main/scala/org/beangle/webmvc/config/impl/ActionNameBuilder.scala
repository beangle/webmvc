package org.beangle.webmvc.config.impl

import org.beangle.commons.lang.Strings.{ substringBeforeLast, unCamel, uncapitalize }
import org.beangle.webmvc.api.annotation.action
import org.beangle.webmvc.config.Profile

object ActionNameBuilder {

  /**
   * Return action name start with /
   */
  def build(clazz: Class[_], profile: Profile): Tuple2[String, String] = {
    val className = clazz.getName
    val ann = clazz.getAnnotation(classOf[action])
    val nameBuilder = new StringBuilder()
    val namespace = new StringBuilder()
    nameBuilder.append(profile.urlPath)

    if (null == ann) {
      if (Profile.SHORT_URI.equals(profile.urlStyle)) {
        val simpleName = className.substring(className.lastIndexOf('.') + 1)
        nameBuilder.append(uncapitalize(simpleName.substring(0, simpleName.length - profile.actionSuffix.length)))
      } else if (Profile.SIMPLE_URI.equals(profile.urlStyle)) {
        nameBuilder.append(profile.getMatched(className))
      } else if (Profile.SEO_URI.equals(profile.urlStyle)) {
        nameBuilder.append(unCamel(profile.getMatched(className)))
      } else {
        throw new RuntimeException("unsupported uri style " + profile.urlStyle)
      }
      namespace ++= nameBuilder.substring(0, nameBuilder.lastIndexOf("/"))
    } else {
      val name = ann.value()
      namespace ++= nameBuilder
      if (!name.startsWith("/")) {
        if (Profile.SEO_URI == profile.urlStyle) {
          val middleName = unCamel(substringBeforeLast(profile.getMatched(className), "/"))
          if (middleName.length > 0) {
            namespace ++= middleName
            nameBuilder ++= middleName
          }
          if (name.length > 0) nameBuilder.append("/" + name)
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