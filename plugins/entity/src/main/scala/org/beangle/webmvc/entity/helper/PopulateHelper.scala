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