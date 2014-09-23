package org.beangle.webmvc.entity.action

import org.beangle.data.model.Entity
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View

abstract class RestfulEntityAction[T <: Entity[_]] extends AbstractEntityAction[T] {

  def index(): String = {
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

  @mapping(value = "{id}/edit")
  def edit(@param("id") id: String): String = {
    var entity = getModel(id)
    editSetting(entity)
    put(shortName, entity)
    forward()
  }

  @mapping(value = "new")
  def editNew(): String = {
    var entity = getEntity(getEntityType, shortName)
    editSetting(entity)
    put(shortName, entity)
    forward("new")
  }

  @mapping(value = "{id}", method = "delete")
  def remove(@param("id") id: String): View = {
    removeAndRedirect(List(getModel(id)))
  }

  def batchRemove(): View = {
    val idclass = entityMetaData.getType(entityName).get.idType
    val entityId = getId(shortName, idclass)
    val entities =
      if (null == entityId) getModels[Object](entityName, getIds(shortName, idclass))
      else List(getModel[Object](entityName, entityId))
    removeAndRedirect(entities)
  }

  @mapping(value = "{id}", method = "put")
  def update(@param("id") id: String): View = {
    val entity = populate(getModel(id), entityName, shortName)
    saveAndRedirect(entity)
  }

  @mapping(method = "post")
  def save(): View = {
    saveAndRedirect(populateEntity())
  }

  @ignore
  protected def indexSetting(): Unit = {}

  @ignore
  protected def editSetting(entity: T): Unit = {}
}