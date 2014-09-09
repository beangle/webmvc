package org.beangle.webmvc.entity.action

import org.beangle.data.model.Entity
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View

abstract class RestfulEntityAction extends AbstractEntityAction {

  def index(): String = {
    forward()
  }

  def search(): String = {
    put(shortName + "s", entityDao.search(getQueryBuilder()))
    forward()
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): String = {
    val entityId = Params.converter.convert(id, entityMetaData.getType(entityName).get.idClass)
    put(shortName, getModel(entityName, entityId))
    forward()
  }

  @mapping(value = "{id}/edit")
  def edit(@param("id") id: String): String = {
    var entity = getEntity
    editSetting(entity)
    put(shortName, entity)
    forward()
  }

  @mapping(value = "new")
  def editNew(): String = {
    var entity = getEntity
    editSetting(entity)
    put(shortName, entity)
    forward("new")
  }

  @mapping(value = "{id}", method = "delete")
  def remove(@param("id") id: String): String = {
    null
  }

  @mapping(value = "{id}", method = "put")
  def update(@param("id") id: String): View = {
    val entity: Entity[_] = getModel(entityName, id)
    populate(entity, entityName, Params.sub(shortName).asInstanceOf[Map[String, Object]])
    super.saveAndRedirect(entity)
  }

  @mapping(method = "post")
  def save(): View = {
    saveAndRedirect(populateEntity())
  }

  @ignore
  protected def indexSetting(): Unit = {}

  @ignore
  protected def editSetting(entity: Entity[_]): Unit = {}
}