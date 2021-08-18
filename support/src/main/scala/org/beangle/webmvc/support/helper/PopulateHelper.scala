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

import org.beangle.commons.lang.Strings
import org.beangle.data.model.Entity
import org.beangle.data.model.meta.EntityType
import org.beangle.data.model.util.ConvertPopulator
import org.beangle.web.action.context.Params
import org.beangle.webmvc.context.ContainerHelper
import org.beangle.data.hibernate.DomainFactory

object PopulateHelper {

  private val domain = ContainerHelper.get.getBean(classOf[DomainFactory]).head.result

  var populator = new ConvertPopulator

  final def getType(clazz: Class[_]): EntityType = {
    domain.getEntity(clazz).get
  }

  final def getType(className: String): EntityType = {
    domain.getEntity(className).get
  }
  /**
   * 将request中的参数设置到clazz对应的bean。
   */
  def populate[T <: Entity[_]](clazz: Class[T], name: String): T = {
    val etype = getType(clazz)
    populate(etype.newInstance().asInstanceOf[T], etype.entityName, name)
  }

  def populate[T <: Entity[_]](clazz: Class[T]): T = {
    val etype = getType(clazz)
    populate(etype.newInstance().asInstanceOf[T], etype.entityName, shortName(etype.entityName))
  }

  def populate(entityName: String): Object = {
    val etype = getType(Class.forName(entityName))
    populate(etype.newInstance().asInstanceOf[Entity[_]], etype.entityName, shortName(etype.entityName))
  }

  def populate(entityName: String, name: String): Object = {
    val etype = getType(Class.forName(entityName))
    val params = Params.sub(name)
    val entity = etype.newInstance().asInstanceOf[Entity[_]]
    populator.populate(entity, etype, params)
    entity
  }

  def populate[T <: Entity[_]](obj: T, entityName: String, name: String): T = {
    val params = Params.sub(name)
    populator.populate(obj, getType(entityName), params)
    obj
  }

  def populate[T <: Entity[_]](obj: T, params: collection.Map[String, Any]): T = {
    populator.populate(obj, getType(obj.getClass), params)
    obj
  }

  def populate[T <: Entity[_]](obj: T, entityName: String, params: collection.Map[String, Any]): T = {
    populator.populate(obj, getType(entityName), params)
    obj
  }

  private def shortName(entityName: String): String = Strings.uncapitalize(Strings.substringAfterLast(entityName, "."))
}
