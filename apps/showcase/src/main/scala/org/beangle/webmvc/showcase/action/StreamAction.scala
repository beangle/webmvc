package org.beangle.webmvc.showcase.action

import java.io.File

import org.beangle.commons.lang.ClassLoaders
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.view.{View, stream}

class StreamAction extends ActionSupport {

  val url = ClassLoaders.getResource("ehcache.xml")
  val file = new File(url.getFile())
  def download(): View = {
    stream(url)
  }

  def download2(): View = {
    stream(url, "text/xml", "index")
  }

  def download3(): View = {
    stream(file, "text/xml", "显示文件名.html")
  }

  def download4(): View = {
    stream(url.openStream(), "text/xml", "显示文件名.html")
  }
}
