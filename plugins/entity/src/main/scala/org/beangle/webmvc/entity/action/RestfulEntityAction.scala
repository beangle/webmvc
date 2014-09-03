package org.beangle.webmvc.entity.action

import org.beangle.commons.config.property.PropertyConfig
import org.beangle.data.model.Entity
import org.beangle.data.model.dao.GeneralDao
import org.beangle.data.model.meta.EntityMetadata
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }
import org.beangle.webmvc.api.context.Params

abstract class RestfulEntityAction extends AbstractEntityAction {

  def search(): String = {
    put(shortName + "s", entityDao.search(getQueryBuilder()))
    forward()
  }

  def index(): String = {
    forward()
  }

  @mapping(value = "{id}", method = "get")
  def info(@param("id") id: String): String = {
    val entityId = Params.converter.convert(id, entityMetaData.getType(entityName).get.idClass)
    put(shortName, getModel(entityName, entityId))
    forward()
  }

  @mapping(value = "{id}/edit", method = "get")
  def edit(@param("id") id: String): String = {
    forward()
  }

  @mapping(value = "new", method = "get")
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