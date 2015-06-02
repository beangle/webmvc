package org.beangle.webmvc.entity.action

import java.{ util => ju }

import org.beangle.data.model.{ Entity, Updated }
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.context.ActionContextHelper

abstract class RestfulAction[T <: Entity[_ <: java.io.Serializable]] extends ActionSupport with EntityAction[T] {

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
    put(shortName, getModel(entityName, convertId(id)))
    forward()
  }

  protected def indexSetting(): Unit = {}

  @mapping(value = "{id}/edit")
  def edit(@param("id") id: String): String = {
    var entity = getModel(id)
    editSetting(entity)
    put(shortName, entity)
    forward()
  }

  @mapping(value = "new", view = "new,form")
  def editNew(): String = {
    var entity = getEntity(entityType, shortName)
    editSetting(entity)
    put(shortName, entity)
    forward()
  }

  @mapping(method = "delete")
  def remove(): View = {
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

  @mapping(method = "post")
  def save(): View = {
    saveAndRedirect(populateEntity())
  }

  @ignore
  protected def saveAndRedirect(entity: T): View = {
    try {
      entity match {
        case updated: Updated => updated.updatedAt = new ju.Date()
        case _ =>
      }
      saveOrUpdate(entity)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        val redirectTo = ActionContextHelper.getMapping(ContextHolder.context).action.method.getName match {
          case "save" => "editNew"
          case "update" => "edit"
        }
        logger.info("saveAndForwad failure", e)
        redirect(redirectTo, "info.save.failure")
      }
    }
  }

  protected def editSetting(entity: T): Unit = {}
}