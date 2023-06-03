/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.support.action

import org.beangle.commons.text.inflector.en.EnNounPluralizer
import org.beangle.data.dao.EntityDao
import org.beangle.data.model.Entity
import org.beangle.data.model.pojo.Updated
import org.beangle.web.action.annotation.{ignore, mapping, param}
import org.beangle.web.action.context.{ActionContext, Params}
import org.beangle.web.action.support.ActionSupport
import org.beangle.web.action.view.View
import org.beangle.webmvc.execution.MappingHandler
import org.beangle.webmvc.support.helper.PopulateHelper

import java.time.Instant

abstract class RestfulAction[T <: Entity[_]] extends ActionSupport,EntityAction[T] {
  var entityDao: EntityDao = _

  def index(): View = {
    indexSetting()
    forward()
  }

  def search(): View = {
    put(EnNounPluralizer.pluralize(simpleEntityName), entityDao.search(getQueryBuilder))
    forward()
  }

  @mapping(value = "{id}")
  def info(@param("id") id: String): View = {
    val entityType = entityDao.domain.getEntity(entityClass).get
    put(simpleEntityName, getModel[T](entityType, convertId(entityType, id)))
    forward()
  }

  protected def indexSetting(): Unit = {}

  @mapping(value = "{id}/edit")
  def edit(@param("id") id: String): View = {
    val entity = getModel(id)
    editSetting(entity)
    put(simpleEntityName, entity)
    forward()
  }

  @mapping(value = "new", view = "new,form")
  def editNew(): View = {
    val entity = getEntity(entityClass, simpleEntityName)
    editSetting(entity)
    put(simpleEntityName, entity)
    forward()
  }

  @mapping(method = "delete")
  def remove(): View = {
    val entityType = entityDao.domain.getEntity(entityClass).get
    val idclass = entityType.id.clazz
    val entities: Seq[T] = Params.getId(simpleEntityName, idclass) match {
      case Some(entityId) => List(getModel[T](entityType, entityId))
      case None => getModels[T](entityType, Params.getIds(simpleEntityName, idclass))
    }
    try {
      removeAndRedirect(entities)
    } catch {
      case e: Exception =>
        logger.info("removeAndRedirect failure", e)
        redirect("search", "info.delete.failure")
    }
  }

  @mapping(value = "{id}", method = "put")
  def update(@param("id") id: String): View = {
    val entity = PopulateHelper.populate(getModel(id), entityDao.domain.getEntity(entityClass).get, simpleEntityName)
    persist(entity)
  }

  @mapping(method = "post")
  def save(): View = {
    persist(populateEntity())
  }

  @ignore
  protected def saveAndRedirect(entity: T): View = {
    saveOrUpdate(entity)
    redirect("search", "info.save.success")
  }

  @ignore
  protected def removeAndRedirect(entities: Seq[T]): View = {
    remove(entities)
    redirect("search", "info.remove.success")
  }

  protected def editSetting(entity: T): Unit = {}

  private def persist(entity: T): View = {
    try {
      entity match {
        case updated: Updated => updated.updatedAt = Instant.now
        case _ =>
      }
      saveAndRedirect(entity)
    } catch {
      case e: Exception =>
        val mapping = ActionContext.current.handler.asInstanceOf[MappingHandler].mapping
        val redirectTo = mapping.method.getName match {
          case "save" => "editNew"
          case "update" => "edit"
        }
        logger.info("saveAndRedirect failure", e)
        redirect(redirectTo, "info.save.failure")
    }
  }

}
