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
package org.beangle.webmvc.api.view

import java.io.{File, FileInputStream, InputStream}
import java.net.URL

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.Strings.{isBlank, substringAfterLast}

object Stream {

  def apply(url: URL): StreamView = {
    val fileName = substringAfterLast(url.toString, "/")
    new StreamView(url.openStream(), decideContentType(fileName), getAttachName(fileName))
  }

  def apply(url: URL, displayName: String): StreamView = {
    val fileName = substringAfterLast(url.toString, "/")
    new StreamView(url.openStream(), decideContentType(fileName), getAttachName(fileName, displayName))
  }

  def apply(url: URL, contentType: String, displayName: String): StreamView = {
    val fileName = substringAfterLast(url.toString, "/")
    new StreamView(url.openStream(), contentType, getAttachName(fileName, displayName))
  }

  def apply(file: File): StreamView = {
    val fileName = file.getName
    new StreamView(new FileInputStream(file), decideContentType(fileName), getAttachName(fileName))
  }

  def apply(file: File, displayName: String): StreamView = {
    val fileName = file.getName
    new StreamView(new FileInputStream(file), decideContentType(fileName), getAttachName(fileName, displayName))
  }

  def apply(file: File, contentType: String, displayName: String): StreamView = {
    new StreamView(new FileInputStream(file), contentType, getAttachName(file.getName, displayName))
  }

  def apply(is: InputStream, contentType: String, displayName: String): StreamView = {
    new StreamView(is, contentType, displayName)
  }

  private def decideContentType(fileName: String): String = {
    MediaTypes.get(substringAfterLast(fileName, "."), MediaTypes.ApplicationOctetStream).toString
  }

  private def getAttachName(name: String, display: String = null): String = {
    var attch_name = ""
    val ext = substringAfterLast(name, ".")
    if (isBlank(display)) {
      attch_name = name
      var iPos = attch_name.lastIndexOf("\\")
      if (iPos > -1) attch_name = attch_name.substring(iPos + 1)
      iPos = attch_name.lastIndexOf("/")
      if (iPos > -1) attch_name = attch_name.substring(iPos + 1)
    } else {
      attch_name = display
      if (!attch_name.contains(".")) attch_name += "." + ext
    }
    attch_name
  }
}

class StreamView(val inputStream: InputStream, val contentType: String, val displayName: String) extends View {

}
