package org.beangle.webmvc.view.template

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.logging.Logging

abstract class AbstractTemplateEngine extends TemplateEngine with Logging {
  val DEFAULT_THEME_PROPERTIES_FILE_NAME = "theme.properties"
  protected val themeProps = new collection.mutable.HashMap[String, Map[String, String]]

  def getThemeProps(theme: String): Map[String, String] = {
    themeProps.get(theme) match {
      case Some(p) => p
      case None =>
        val props = IOs.readJavaProperties(ClassLoaders.getResourceAsStream(Strings.concat("template/", theme, "/", DEFAULT_THEME_PROPERTIES_FILE_NAME), getClass))
        themeProps.put(theme, props)
        props
    }
  }

  def getParentTemplate(template: String): String = {
    val start = template.indexOf('/', 1) + 1
    val end = template.lastIndexOf('/')

    getThemeProps(template.substring(start, end)).get("parent") match {
      case Some(parentTheme) => Strings.concat(template.substring(0, start), parentTheme, template.substring(end))
      case None => null
    }
  }
}