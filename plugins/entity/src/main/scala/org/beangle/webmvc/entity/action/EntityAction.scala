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

import java.{ io => jo }
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.config.property.PropertyConfig
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.model.meta.{ EntityMetadata, EntityType }
import org.beangle.webmvc.api.action.{ EntitySupport, ParamSupport, RouteSupport }
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.helper.{ PopulateHelper, QueryHelper }

trait EntityAction[T <: Entity[_]] extends RouteSupport with ParamSupport with EntitySupport[T] with Logging {
  var entityDao: EntityDao = _
  var config: PropertyConfig = _
  var entityMetaData: EntityMetadata = _

  /**
   * 将request中的参数设置到clazz对应的bean。
   */
  protected final def populate[E <: Entity[_]](clazz: Class[E], shortName: String): E = {
    PopulateHelper.populate(clazz, shortName);
  }

  protected final def populate[E <: Entity[_]](obj: E, shortName: String): E = {
    PopulateHelper.populate(obj, Params.sub(shortName))
  }

  protected final def populate[E <: Entity[_]](clazz: Class[E]): E = {
    PopulateHelper.populate(clazz);
  }

  protected final def populate(entityName: String) = PopulateHelper.populate(entityName);

  protected final def populate(entityName: String, shortName: String): Object = {
    PopulateHelper.populate(entityName, shortName);
  }

  protected final def populate[E <: Entity[_]](obj: E, entityName: String, shortName: String): E = {
    PopulateHelper.populate(obj, entityName, shortName)
  }

  protected final def populate[E <: Entity[_]](entity: E, entityName: String, params: collection.Map[String, Any]): E = {
    require(null != entity, "Cannot populate to null.")
    PopulateHelper.populate(entity, entityName, params)
  }

  protected final def populate[E <: Entity[_]](entity: E, params: Map[String, Object]): E = {
    require(null != entity, "Cannot populate to null.")
    PopulateHelper.populate(entity, params)
  }

  // query------------------------------------------------------
  protected final def getPageIndex: Int = {
    QueryHelper.pageIndex
  }

  protected final def getPageSize: Int = {
    QueryHelper.pageSize
  }

  /**
   * 从request的参数或者cookie中(参数优先)取得分页信息
   */
  protected final def getPageLimit: PageLimit = {
    QueryHelper.pageLimit
  }

  protected final def populateConditions(builder: OqlBuilder[_]): Unit = {
    QueryHelper.populateConditions(builder);
  }

  protected final def populateConditions(builder: OqlBuilder[_], exclusiveAttrNames: String): Unit = {
    QueryHelper.populateConditions(builder, exclusiveAttrNames);
  }

  @ignore
  final def entityName: String = {
    entityType.getName
  }

  @ignore
  protected def shortName: String = {
    Strings.uncapitalize(Strings.substringAfterLast(entityName, "."))
  }

  // CURD----------------------------------------
  protected def remove[E](list: Seq[E]): Unit = {
    entityDao.remove(list)
  }

  protected def remove[E](obj: E): Unit = {
    entityDao.remove(obj)
  }

  protected def saveOrUpdate[E](list: Iterable[E]): Unit = {
    entityDao.saveOrUpdate(list)
  }

  protected def saveOrUpdate[E](obj: E): Unit = {
    entityDao.saveOrUpdate(obj)
  }

  protected def getQueryBuilder(): OqlBuilder[T] = {
    val builder: OqlBuilder[T] = OqlBuilder.from(entityName, shortName)
    populateConditions(builder)
    builder.orderBy(get(Order.OrderStr).orNull).limit(getPageLimit)
  }

  protected def populateEntity(): T = {
    populateEntity(entityName, shortName).asInstanceOf[T]
  }

  protected def populateEntity[E <: Entity[_]](entityName: String, shortName: String): E = {
    getId(shortName, entityMetaData.getType(entityName).get.idType) match {
      case Some(entityId) => populate(getModel[E](entityName, entityId), entityName, Params.sub(shortName))
      case None           => populate(entityName, shortName).asInstanceOf[E]
    }
  }

  protected def populateEntity[E](entityClass: Class[E], shortName: String): E = {
    val entityType =
      if (entityClass.isInterface) entityMetaData.getType(entityClass.getName)
      else entityMetaData.getType(entityClass)
    populateEntity(entityType.get.entityName, shortName)
  }

  protected def getEntity[E <: Entity[_]](entityName: String, name: String): E = {
    val entityType: EntityType = entityMetaData.getType(entityName).get
    getId(name, entityType.idType) match {
      case Some(entityId) => getModel(entityName, entityId).asInstanceOf[E]
      case None           => populate(entityType.newInstance.asInstanceOf[E], entityType.entityName, name)
    }
  }

  protected def getEntity[E](entityClass: Class[E], shortName: String): E = {
    val entityType: EntityType =
      (if (entityClass.isInterface) entityMetaData.getType(entityClass.getName)
      else entityMetaData.getType(entityClass)).get
    getEntity(entityType.entityName, shortName).asInstanceOf[E]
  }

  protected def getModel(id: jo.Serializable): T = {
    getModel[T](entityName, id)
  }

  protected def getModel[E](entityName: String, id: Any): E = {
    val entityType: EntityType = entityMetaData.getType(entityName).get
    Params.converter.convert(id, entityType.idType) match {
      case Some(rid) => entityDao.get(entityType.entityClass.asInstanceOf[Class[Entity[jo.Serializable]]], rid).asInstanceOf[E]
      case None      => null.asInstanceOf[E]
    }
  }

  protected def getModels[E](entityName: String, ids: Iterable[_]): Seq[E] = {
    val idlist = ids.asInstanceOf[List[jo.Serializable]]
    entityDao.find(Class.forName(entityName).asInstanceOf[Class[Entity[jo.Serializable]]], idlist).asInstanceOf[Seq[E]]
  }

  protected def convertId[ID](id: String): ID = {
    Params.converter.convert(id, entityMetaData.getType(entityName).get.idType) match {
      case None      => null.asInstanceOf[ID]
      case Some(nid) => nid.asInstanceOf[ID]
    }
  }

  @ignore
  protected def removeAndRedirect(entities: Seq[T]): View = {
    try {
      remove(entities)
      redirect("search", "info.remove.success")
    } catch {
      case e: Exception => {
        logger.info("removeAndForwad failure", e)
        redirect("search", "info.delete.failure")
      }
    }
  }
}
