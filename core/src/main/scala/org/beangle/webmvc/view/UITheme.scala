package org.beangle.webmvc.view

/**
 * UITheme represent css or images resource bundle name.
 *
 * @author chaostone
 * @since 3.0.0
 */
class UITheme(val base: String) {

  def iconurl(themeName: String,name: String): String = {
    iconurl(themeName,name, "16x16")
  }

  def iconurl(themeName: String,name: String, size: Integer): String = {
    val sb = new StringBuilder()
    sb.append(size).append('x').append(size)
    iconurl(themeName,name, sb.toString())
  }

  def iconurl(themeName: String,name: String, size: String): String = {
    val sb = new StringBuilder(80)
    sb.append(base)
    sb.append(themeName).append("/icons/").append(size)
    if (!name.startsWith("/")) sb.append('/')
    sb.append(name)
    sb.toString()
  }

  def cssurl(themeName: String,name: String): String = {
    val sb = new StringBuilder(80)
    sb.append(themeName)
    if (!name.startsWith("/")) sb.append('/')
    sb.append(name)
    sb.toString()
  }


}