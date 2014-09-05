package org.beangle.webmvc.entity.action

import org.beangle.commons.config.property.PropertyConfig
import org.beangle.data.model.Entity
import org.beangle.data.model.dao.GeneralDao
import org.beangle.data.model.meta.EntityMetadata
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View

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
    var entity = getEntity
    editSetting(entity)
    put(shortName, entity)
    forward("form")
  }

  @mapping(value = "{id}", method = "delete")
  def remove(@param("id") id: String): String = {
    null
  }

  @mapping(value = "{id}", method = "put")
  def update(@param("id") id: String): String = {
    null
  }

  @mapping(value = "", method = "post")
  def save(): View = {
    saveAndRedirect(populateEntity())
  }

  protected def indexSetting(): Unit = {}

  protected def editSetting(entity: Entity[_]): Unit = {}
}