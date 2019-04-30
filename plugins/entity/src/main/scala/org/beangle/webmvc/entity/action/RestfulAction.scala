/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.entity.action

import java.time.Instant

import org.beangle.commons.text.inflector.en.EnNounPluralizer
import org.beangle.data.model.Entity
import org.beangle.data.model.pojo.Updated
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{ignore, mapping, param}
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.execution.Handler

abstract class RestfulAction[T <: Entity[_]] extends ActionSupport
  with EntityAction[T] with ExportSupport[T] with ImportSupport[T] {

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
    put(simpleEntityName, getModel[T](entityName, convertId(id)))
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
    val entity = getEntity(entityType, simpleEntityName)
    editSetting(entity)
    put(simpleEntityName, entity)
    forward()
  }

  @mapping(method = "delete")
  def remove(): View = {
    val idclass = entityDao.domain.getEntity(entityName).get.id.clazz
    val entities: Seq[T] = getId(simpleEntityName, idclass) match {
      case Some(entityId) => List(getModel[T](entityName, entityId))
      case None           => getModels[T](entityName, ids(simpleEntityName, idclass))
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
    val entity = populate(getModel(id), entityName, simpleEntityName)
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
        case _                =>
      }
      saveAndRedirect(entity)
    } catch {
      case e: Exception =>
        val redirectTo = Handler.mapping.method.getName match {
          case "save"   => "editNew"
          case "update" => "edit"
        }
        logger.info("saveAndRedirect failure", e)
        redirect(redirectTo, "info.save.failure")
    }
  }

}
