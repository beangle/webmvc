package org.beangle.webmvc.entity.action

import org.beangle.data.model.Entity
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View

abstract class RestfulAction[T <: Entity[_ <: java.io.Serializable]] extends AbstractRestfulAction[T] {

  @mapping(value = "{id}/edit")
  def edit(@param("id") id: String): String = {
    var entity = getModel(id)
    editSetting(entity)
    put(shortName, entity)
    forward()
  }

  @mapping(value = "new")
  def editNew(): String = {
    var entity = getEntity(entityType, shortName)
    editSetting(entity)
    put(shortName, entity)
    forward("new")
  }

  @mapping(value = "{id}", method = "delete")
  def remove(@param("id") id: String): View = {
    removeAndRedirect(List(getModel(id)))
  }

  @mapping(method = "delete")
  def batchRemove(): View = {
    val idclass = entityMetaData.getType(entityName).get.idType
    val entityId = getId(shortName, idclass)
    val entities: Seq[T] =
      if (null == entityId) getModels(entityName, getIds(shortName, idclass))
      else List(getModel(entityName, entityId))
    removeAndRedirect(entities)
  }

  @mapping(value = "{id}", method = "put")
  def update(@param("id") id: String): View = {
    val entity = populate(getModel(id), entityName, shortName)
    saveAndRedirect(entity)
  }

  @mapping(method = "put")
  def batchUpdate(@param("id") id: String): View = {
    val entity = populate(getModel(id), entityName, shortName)
    saveAndRedirect(entity)
  }

  @mapping(method = "post")
  def save(): View = {
    saveAndRedirect(populateEntity())
  }

  protected def editSetting(entity: T): Unit = {}
}