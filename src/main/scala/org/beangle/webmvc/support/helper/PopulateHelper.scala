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

package org.beangle.webmvc.support.helper

import org.beangle.commons.cdi.Container
import org.beangle.commons.lang.Strings
import org.beangle.data.model.Entity
import org.beangle.data.model.meta.EntityType
import org.beangle.data.model.util.ConvertPopulator
import org.beangle.data.orm.Jpas
import org.beangle.data.orm.hibernate.DomainFactory
import org.beangle.webmvc.context.Params

object PopulateHelper {

  private val domain = Container.get("web").getBean(classOf[DomainFactory]).head.result

  var populator = new ConvertPopulator

  final def getType(clazz: Class[_]): EntityType = {
    domain.getEntity(clazz).get
  }

  final def getType(obj: Entity[_]): EntityType = {
    domain.getEntity(Jpas.entityClass(obj)).get
  }

  /**
   * 将request中的参数设置到clazz对应的bean。
   */
  def populate[T <: Entity[_]](clazz: Class[T], name: String): T = {
    val etype = getType(clazz)
    populate(etype.newInstance().asInstanceOf[T], etype, name)
  }

  def populate[T <: Entity[_]](clazz: Class[T]): T = {
    val etype = getType(clazz)
    populate(etype.newInstance().asInstanceOf[T], etype, shortName(etype.entityName))
  }

  def populate(entityType: EntityType, name: String): Object = {
    val params = Params.sub(name)
    val entity = entityType.newInstance().asInstanceOf[Entity[_]]
    populator.populate(entity, entityType, params)
    entity
  }

  def populate[T <: Entity[_]](obj: T, entityType: EntityType, name: String): T = {
    val params = Params.sub(name)
    populator.populate(obj, entityType, params)
    obj
  }

  def populate[T <: Entity[_]](obj: T, params: collection.Map[String, Any]): T = {
    populator.populate(obj, getType(obj), params)
    obj
  }

  def populate[T <: Entity[_]](obj: T, entityType: EntityType, params: collection.Map[String, Any]): T = {
    populator.populate(obj, entityType, params)
    obj
  }

  private def shortName(entityName: String): String = Strings.uncapitalize(Strings.substringAfterLast(entityName, "."))
}
