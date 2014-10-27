package org.beangle.webmvc.api.view

import java.io.{ File, FileInputStream, InputStream }
import java.net.URL

import org.beangle.commons.activation.{ MimeTypeProvider, MimeTypes }
import org.beangle.commons.lang.Strings.{ isBlank, substringAfterLast }

object Stream {

  def apply(url: URL): StreamView = {
    val fileName = substringAfterLast(url.toString(), "/")
    new StreamView(url.openStream(), decideContentType(fileName), getAttachName(fileName))
  }

  def apply(url: URL, displayName: String): StreamView = {
    val fileName = substringAfterLast(url.toString(), "/")
    new StreamView(url.openStream(), decideContentType(fileName), getAttachName(fileName, displayName))
  }

  def apply(url: URL, contentType: String, displayName: String): StreamView = {
    val fileName = substringAfterLast(url.toString(), "/")
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
    MimeTypeProvider.getMimeType(substringAfterLast(fileName, "."), MimeTypes.ApplicationOctetStream).toString
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