package org.beangle.webmvc.entity.action

import org.beangle.data.model.Entity
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View

abstract class AbstractRestfulAction[T <: Entity[_ <: java.io.Serializable]] extends AbstractEntityAction[T] {

  def index(): String = {
    indexSetting()
    forward()
  }

  def search(): String = {
    put(shortName + "s", entityDao.search(getQueryBuilder()))
    forward()
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): String = {
    val entityId = Params.converter.convert(id, entityMetaData.getType(entityName).get.idType)
    put(shortName, getModel(entityName, entityId))
    forward()
  }

  protected def indexSetting(): Unit = {}

}