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

import org.beangle.commons.activation.MediaTypes
import org.beangle.commons.lang.Strings.{isBlank, substringAfterLast}

import java.io.{File, FileInputStream, InputStream}
import java.net.URL

object Stream {

  def apply(url: URL): StreamView = {
    val fileName = substringAfterLast(url.toString, "/")
    apply(url, fileName)
  }

  def apply(url: URL, displayName: String): StreamView = {
    val fileName = substringAfterLast(url.toString, "/")
    apply(url, decideContentType(fileName), displayName)
  }

  def apply(url: URL, contentType: String, displayName: String): StreamView = {
    val fileName = substringAfterLast(url.toString, "/")
    val conn = url.openConnection()
    new StreamView(conn.getInputStream, contentType, getAttachName(fileName, displayName), None, Some(conn.getLastModified))
  }

  def apply(file: File): StreamView = {
    val fileName = file.getName
    apply(file, decideContentType(fileName), getAttachName(fileName))
  }

  def apply(file: File, displayName: String): StreamView = {
    val fileName = file.getName
    apply(file, decideContentType(fileName), displayName)
  }

  def apply(file: File, contentType: String, displayName: String): StreamView = {
    new StreamView(new FileInputStream(file), contentType, getAttachName(file.getName, displayName), Some(file.length()), Some(file.lastModified()))
  }

  def apply(is: InputStream, contentType: String, displayName: String, contentLength: Option[Long], lastModified: Option[Long]): StreamView = {
    new StreamView(is, contentType, displayName, contentLength, lastModified)
  }

  private def decideContentType(fileName: String): String = {
    MediaTypes.get(substringAfterLast(fileName, "."), MediaTypes.ApplicationOctetStream).toString
  }

  private def getAttachName(fileName: String, display: String = null): String = {
    var attch_name = ""
    val ext = substringAfterLast(fileName, ".")
    if (isBlank(display)) {
      attch_name = fileName
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

class StreamView(val inputStream: InputStream, val contentType: String, val displayName: String, val contentLength: Option[Long], val lastModified: Option[Long]) extends View {

}
