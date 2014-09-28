package org.beangle.webmvc.showcase.action

import org.beangle.webmvc.api.annotation.body

class SerialAction {

  @body
  def data(): Map[String, Object] = {
    Map("key1" -> new java.lang.Long(9), "key2" -> List(3, 3))
  }
}