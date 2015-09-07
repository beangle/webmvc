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
package org.beangle.webmvc.entity.helper

import org.beangle.commons.lang.Strings
import org.beangle.data.model.Entity
import org.beangle.data.model.meta.{ EntityMetadata, EntityType }
import org.beangle.data.model.util.ConvertPopulator
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.context.ContainerHelper

object PopulateHelper {

  var metadata: EntityMetadata = ContainerHelper.get.getBean(classOf[EntityMetadata]).head

  var populator = new ConvertPopulator

  final def getType(clazz: Class[_]): EntityType = {
    metadata.getType(clazz).getOrElse(new EntityType(clazz, clazz.getName, "id"))
  }
  /**
   * 将request中的参数设置到clazz对应的bean。
   */
  def populate[T <: Entity[_]](clazz: Class[T], name: String): T = {
    val etype = getType(clazz)
    populate(etype.newInstance().asInstanceOf[T], etype.entityName, name).asInstanceOf[T]
  }

  def populate[T <: Entity[_]](clazz: Class[T]): T = {
    val etype = getType(clazz)
    populate(etype.newInstance().asInstanceOf[T], etype.entityName, shortName(etype.entityName)).asInstanceOf[T]
  }

  def populate(entityName: String): Object = {
    val etype = getType(Class.forName(entityName))
    populate(etype.newInstance().asInstanceOf[Entity[_]], etype.entityName, shortName(etype.entityName))
  }

  def populate(entityName: String, name: String): Object = {
    val etype = getType(Class.forName(entityName))
    var params = Params.sub(name)
    val entity = etype.newInstance().asInstanceOf[Entity[_]]
    populator.populate(entity, etype, params)
    entity
  }

  def populate[T <: Entity[_]](obj: T, entityName: String, name: String): T = {
    var params = Params.sub(name)
    populator.populate(obj, getType(obj.getClass), params)
    obj
  }

  def populate[T <: Entity[_]](obj: T, params: collection.Map[String, Any]): T = {
    populator.populate(obj, getType(obj.getClass), params)
    obj
  }

  def populate[T <: Entity[_]](obj: T, entityName: String, params: collection.Map[String, Any]): T = {
    val etype = metadata.getType(entityName).get
    populator.populate(obj, etype, params)
    obj
  }

  private def shortName(entityName: String): String = Strings.uncapitalize(Strings.substringAfterLast(entityName, "."))
}