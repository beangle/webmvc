/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.entity.action

import java.{ util => ju }
import org.beangle.data.model.{ Entity, Updated }
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }
import org.beangle.webmvc.api.context.ActionContextHolder
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.context.ActionContextHelper

abstract class RestfulAction[T <: Entity[_]] extends ActionSupport with EntityAction[T] {

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
    put(shortName, getModel[T](entityName, convertId(id)))
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
    val entities: Seq[T] = getId(shortName, idclass) match {
      case Some(entityId) => List(getModel[T](entityName, entityId))
      case None           => getModels[T](entityName, ids(shortName, idclass))
    }
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
        case _                =>
      }
      saveOrUpdate(entity)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        val redirectTo = ActionContextHelper.mapping.method.getName match {
          case "save"   => "editNew"
          case "update" => "edit"
        }
        logger.info("saveAndForwad failure", e)
        redirect(redirectTo, "info.save.failure")
      }
    }
  }

  protected def editSetting(entity: T): Unit = {}
}