package org.beangle.webmvc.view

object UITheme {
  /**
   * Registe all ui theme
   */
  private val themes = new collection.mutable.HashMap[String, UITheme]()

  def getTheme(name: String, base: String): UITheme = {
    themes.get(name).getOrElse({
      val theme: UITheme = new UITheme(name, base)
      themes.put(name, theme)
      theme
    })
  }
}

/**
 * UITheme represent css or images resource bundle name.
 *
 * @author chaostone
 * @since 3.0.0
 */
class UITheme(val name: String, val base: String) {

  def iconurl(name: String): String = {
    iconurl(name, "16x16")
  }

  def iconurl(name: String, size: Integer): String = {
    val sb = new StringBuilder()
    sb.append(size).append('x').append(size)
    iconurl(name, sb.toString())
  }

  def iconurl(name: String, size: String): String = {
    val sb = new StringBuilder(80)
    if (base.length() < 2) {
      sb.append("/static/themes/")
    } else {
      sb.append(base).append("/static/themes/")
    }
    sb.append(getName()).append("/icons/").append(size)
    if (!name.startsWith("/")) sb.append('/')
    sb.append(name)
    sb.toString()
  }

  def cssurl(name: String): String = {
    val sb = new StringBuilder(80)
    if (base.length() < 2) {
      sb.append("/static/themes/")
    } else {
      sb.append(base).append("/static/themes/")
    }
    sb.append(getName())
    if (!name.startsWith("/")) sb.append('/')
    sb.append(name)
    sb.toString()
  }

  def getName() = name

  def getBase() = base

}