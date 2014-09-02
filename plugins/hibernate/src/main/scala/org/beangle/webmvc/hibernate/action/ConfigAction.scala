package org.beangle.webmvc.hibernate.action

import java.io.FileInputStream
import org.beangle.commons.io.IOs
import org.beangle.data.jpa.hibernate.tool.HbmGenerator
import java.io.File

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