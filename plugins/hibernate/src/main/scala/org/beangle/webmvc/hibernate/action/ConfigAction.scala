package org.beangle.webmvc.hibernate.action

import java.io.{ File, FileInputStream }

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.description
import org.beangle.data.jpa.hibernate.tool.HbmGenerator

@description("Hibernate配置查看器")
class ConfigAction extends AbstractHibernateAction {

  def index(): String = {
    new HbmGenerator().gen("/tmp")
    val file = new File("/tmp/hibernate.hbm.xml")
    val hbm = IOs.readString(new FileInputStream(file))
    put("hbm", hbm)
    file.delete()
    forward()
  }
}