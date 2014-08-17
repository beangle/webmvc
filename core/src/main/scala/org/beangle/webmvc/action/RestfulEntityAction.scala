package org.beangle.webmvc.action

import org.beangle.commons.config.property.PropertyConfig
import org.beangle.data.model.Entity
import org.beangle.data.model.dao.GeneralDao
import org.beangle.data.model.meta.EntityMetadata
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.helper.Params

abstract class RestfulEntityAction extends EntityActionSupport {

  var entityDao: GeneralDao = _
  var config: PropertyConfig = _
  var entityMetaData: EntityMetadata = _

  def index(): String = {
    forward()
  }

  @mapping("{id}")
  def info(@param("id") id: String): String = {
    val entityId = Params.converter.convert(id, entityMetaData.getType(entityName).get.idClass)
    put(shortName, getModel(entityName, entityId))
    forward()
  }

  @mapping("{id}/edit")
  def edit(@param("id") id: String): String = {
    forward()
  }

  @mapping("new")
  def editNew(): String = {
    forward()
  }

  @mapping(value = "{id}", method = "delete")
  def remove(@param("id") id: String): String = {
    null
  }

  @mapping(value = "{id}", method = "put")
  def update(@param("id") id: String): String = {
    null
  }

  protected def getModel[T](entityName: String, id: Serializable): Entity[T] = {
    entityDao.get(Class.forName(entityName).asInstanceOf, id)
  }

  protected def getModels(entityName: String, ids: Array[_]): List[_] = {
    entityDao.find(Class.forName(entityName).asInstanceOf, "id", ids).asInstanceOf[List[_]]
  }
}