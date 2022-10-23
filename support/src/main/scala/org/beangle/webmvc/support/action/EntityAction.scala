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

import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.logging.Logging
import org.beangle.data.dao.{EntityDao, OqlBuilder}
import org.beangle.data.model.Entity
import org.beangle.data.model.meta.EntityType
import org.beangle.web.action.context.Params
import org.beangle.web.action.support.{EntitySupport, ParamSupport, RouteSupport}
import org.beangle.webmvc.support.helper.{PopulateHelper, QueryHelper}

import java.io as jo

trait EntityAction[T <: Entity[_]] extends RouteSupport with ParamSupport with EntitySupport[T] with Logging {
  def entityDao: EntityDao

  protected final def populate[E <: Entity[_]](clazz: Class[E]): E = {
    PopulateHelper.populate(clazz)
  }

  /**
   * 将request中的参数设置到clazz对应的bean。
   */
  protected final def populate[E <: Entity[_]](clazz: Class[E], simpleName: String): E = {
    PopulateHelper.populate(clazz, simpleName)
  }

  protected final def populate[E <: Entity[_]](obj: E, simpleName: String): E = {
    PopulateHelper.populate(obj, Params.sub(simpleName))
  }

  protected final def populate[E <: Entity[_]](entity: E, params: Map[String, Object]): E = {
    require(null != entity, "Cannot populate to null.")
    PopulateHelper.populate(entity, params)
  }

  // query------------------------------------------------------

  /**
   * 从request的参数或者cookie中(参数优先)取得分页信息
   */
  protected final def getPageLimit: PageLimit = {
    QueryHelper.pageLimit
  }

  protected final def populateConditions(builder: OqlBuilder[_]): Unit = {
    QueryHelper.populate(builder)
  }

  protected final def populateConditions(builder: OqlBuilder[_], exclusiveAttrNames: String): Unit = {
    QueryHelper.populate(builder, exclusiveAttrNames)
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

  protected def getQueryBuilder: OqlBuilder[T] = {
    val alias = simpleEntityName
    val builder = OqlBuilder.from(entityClass, alias)
    populateConditions(builder)
    QueryHelper.sort(builder)
    builder.tailOrder(alias + ".id")
    builder.limit(getPageLimit)
  }

  protected def populateEntity(): T = {
    populateEntity(entityDao.domain.getEntity(entityClass).get, simpleEntityName).asInstanceOf[T]
  }

  protected def populateEntity[E <: Entity[_]](entityType: EntityType, simpleEntityName: String): E = {
    getId(simpleEntityName, entityType.id.clazz) match {
      case Some(entityId) => PopulateHelper.populate(getModel[E](entityType, entityId), entityType, Params.sub(simpleEntityName))
      case None => PopulateHelper.populate(entityType, simpleEntityName).asInstanceOf[E]
    }
  }

  protected def populateEntity[E](entityClass: Class[E], simpleEntityName: String): E = {
    val entityType =
      if (entityClass.isInterface) entityDao.domain.getEntity(entityClass.getName)
      else entityDao.domain.getEntity(entityClass)
    populateEntity(entityType.get, simpleEntityName)
  }

  private def getEntity[E <: Entity[_]](entityType: EntityType, simpleName: String): E = {
    getId(simpleName, entityType.id.clazz) match {
      case Some(entityId) => getModel(entityType, entityId).asInstanceOf[E]
      case None => PopulateHelper.populate(entityType.newInstance().asInstanceOf[E], entityType, simpleName)
    }
  }

  protected def getEntity[E](entityClass: Class[E], simpleName: String): E = {
    val entityType: EntityType =
      (if (entityClass.isInterface) entityDao.domain.getEntity(entityClass.getName)
      else entityDao.domain.getEntity(entityClass)).get
    getEntity(entityType, simpleName).asInstanceOf[E]
  }

  protected def getModel(id: Any): T = {
    getModel[T](entityDao.domain.getEntity(entityClass).get, id)
  }

  protected def getModel[E](entityType: EntityType, id: Any): E = {
    val idType = entityType.id.clazz
    Params.converter.convert(id, idType) match {
      case Some(rid) =>
        val classE = entityType.clazz.asInstanceOf[Class[Entity[jo.Serializable]]]
        entityDao.get(classE, rid.asInstanceOf[jo.Serializable]).asInstanceOf[E]
      case None => null.asInstanceOf[E]
    }
  }

  protected def getModels[E](entityType: EntityType, ids: Iterable[_]): Seq[E] = {
    val idlist = ids.asInstanceOf[Iterable[jo.Serializable]]
    val clazz = entityType.clazz.asInstanceOf[Class[Entity[jo.Serializable]]]
    entityDao.find(clazz, idlist).asInstanceOf[Seq[E]]
  }

  protected def convertId[ID](entityType: EntityType, id: String): ID = {
    Params.converter.convert(id, entityType.id.clazz) match {
      case None => null.asInstanceOf[ID]
      case Some(nid) => nid.asInstanceOf[ID]
    }
  }
}
