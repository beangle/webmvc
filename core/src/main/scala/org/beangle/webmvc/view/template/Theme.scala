package org.beangle.webmvc.view.template

import org.beangle.commons.lang.Strings
import com.sun.xml.internal.ws.wsdl.writer.document.Import
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.io.IOs
import org.beangle.commons.io.ResourcePatternResolver

object Themes {

  val Default = new Theme("html")

  val themes = loadThemeProps()

  def getParentTemplate(template: String): String = {
    val start = template.indexOf('/', 1) + 1
    val end = template.lastIndexOf('/')

    themes(template.substring(start, end)).parent match {
      case Some(parentTheme) => Strings.concat(template.substring(0, start), parentTheme, template.substring(end))
      case None => null
    }
  }

  private def loadThemeProps(): Map[String, Theme] = {
    val themePropMap = new collection.mutable.HashMap[String, Theme]
    val resolver = new ResourcePatternResolver
    val urls = resolver.getResources("template/*/theme.properties")
    urls foreach { url =>
      val themeName = Strings.substringBetween(url.getPath(), "template/", "/theme.properties")
      val theme = new Theme(themeName)
      theme.parent = IOs.readJavaProperties(url).get("parent")
      themePropMap.put(themeName, theme)
    }
    themePropMap.toMap
  }

  def apply(name: String): Theme = {
    themes(name)
  }

}

/**
 * name: Theme's name ,html,list,xhtml etc.
 */
class Theme(val name: String) {

  var parent: Option[String] = None

  def getTemplatePath(clazz: Class[_], suffix: String): String = {
    val sb = new StringBuilder(20)
    sb.append("/template/").append(name).append('/').append(Strings.uncapitalize(clazz.getSimpleName)).append(suffix)
    sb.toString()
  }

  override def equals(obj: Any): Boolean = name.equals(obj.toString())

  override def toString() = name

  override def hashCode() = name.hashCode()
}