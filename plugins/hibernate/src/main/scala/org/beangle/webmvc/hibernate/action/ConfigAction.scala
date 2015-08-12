package org.beangle.webmvc.hibernate.action

import java.io.{ File, FileInputStream }
import java.net.URL

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.annotation.action

@description("Hibernate配置查看器")
@action("config/{session_factory_id}")
class ConfigAction extends AbstractAction {

  def index(): String = {
    put("factory", getFactory)
    put("action", this)
    forward()
  }

  def getURLString(url: URL): String = {
    IOs.readString(url.openStream())
  }

}