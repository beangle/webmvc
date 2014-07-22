package org.beangle.webmvc.view.template

import org.beangle.commons.lang.Strings
import com.sun.xml.internal.ws.wsdl.writer.document.Import

/**
 * FIXME 1.mutable => immutable
 *       2. build theme hierarchy relation
 */
object Themes {
  val Default = new Theme("html5")
  /**
   * Default tagName corresponding TagClass
   *
   * @see getTemplateName
   */
  private val tagTemplateNames = new collection.mutable.HashMap[Class[_], String]

  /**
   * Registe all theme
   */
  val themes = new collection.mutable.HashMap[String, Theme]

  def apply(name: String): Theme = {
    themes.get(name).getOrElse({
      val theme = new Theme(name)
      themes.put(name, theme)
      theme
    })
  }

  def getTemplateName(clazz: Class[_]): String = {
    tagTemplateNames.get(clazz).getOrElse({
      val name = Strings.uncapitalize(clazz.getSimpleName())
      tagTemplateNames.put(clazz, name)
      name
    })
  }

}

/**
 * name: Theme's name ,xml,list,xhtml etc.
 */
class Theme(val name: String) {

  def getTemplatePath(clazz: Class[_], suffix: String): String = {
    val sb = new StringBuilder(20)
    sb.append("/template/").append(name).append('/').append(Themes.getTemplateName(clazz)).append(suffix)
    sb.toString()
  }

  override def equals(obj: Any): Boolean = name.equals(obj.toString())

  override def toString() = name

  override def hashCode() = name.hashCode()
}