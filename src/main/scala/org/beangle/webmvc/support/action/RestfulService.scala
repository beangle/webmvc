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

import org.beangle.data.dao.EntityDao
import org.beangle.data.model.Entity
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.context.Params
import org.beangle.webmvc.support.{ActionSupport, MimeSupport}

class RestfulService[T <: Entity[_]] extends ActionSupport with EntityAction[T] with MimeSupport {
  var entityDao: EntityDao = _

  @response
  def index(): Any = {
    getInt("page") match {
      case Some(_) => entityDao.search(getQueryBuilder)
      case None => entityDao.search(getQueryBuilder.limit(null))
    }
  }

  @response
  @mapping(value = "{id}")
  def info(@param("id") id: String): T = {
    val entityType = entityDao.domain.getEntity(entityClass).get
    Params.converter.convert(id, entityType.id.clazz) match {
      case None => null.asInstanceOf[T]
      case Some(entityId) => getModel[T](entityType, entityId)
    }
  }

}
