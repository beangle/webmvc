package org.beangle.webmvc.showcase.action

import java.io.File

import org.beangle.commons.lang.ClassLoaders
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.view.{View, Stream}

class StreamAction extends ActionSupport {

  val url = ClassLoaders.getResource("ehcache.xml")
  val file = new File(url.getFile())
  def download(): View = {
    Stream(url)
  }

  def download2(): View = {
    Stream(url, "text/xml", "index")
  }

  def download3(): View = {
    Stream(file, "text/xml", "显示文件名.html")
  }

  def download4(): View = {
    Stream(url.openStream(), "text/xml", "显示文件名.html")
  }
}
