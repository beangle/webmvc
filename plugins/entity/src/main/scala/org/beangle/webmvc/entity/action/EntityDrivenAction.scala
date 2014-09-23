package org.beangle.webmvc.entity.action

import java.{ util => ju }

import org.beangle.commons.collection.Order
import org.beangle.commons.config.property.PropertyConfig
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.data.model.bean.UpdatedBean
import org.beangle.data.model.dao.{ GeneralDao, QueryBuilder }
import org.beangle.data.model.meta.{ EntityMetadata, EntityType }
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View

abstract class EntityDrivenAction[T <: Entity[_]] extends AbstractEntityAction[T] {

  /**
   * main page
   */
  def index(): String = {
    indexSetting()
    forward()
  }

  /**
   * Seach Entitis
   */
  def search(): String = {
    put(shortName + "s", entityDao.search(getQueryBuilder()))
    forward()
  }

  def getExportDatas[T](): Seq[T] = {
    // 自动会考虑页面是否传入id
    val result: Option[Seq[T]] = if (Strings.isNotBlank(entityName)) {
      entityMetaData.getType(entityName) match {
        case Some(entityType) => {
          val ids = getIds(shortName, entityType.idType).asInstanceOf[Array[Serializable]]
          if (ids != null && ids.length > 0) {
            Some(entityDao.get(entityType.entityClass, ids).asInstanceOf)
          } else None
        }
        case _ => None
      }
    } else None
    result.getOrElse(entityDao.search(getQueryBuilder().limit(null)).asInstanceOf)
  }
  /**
   * Edit by entity.id or id
   */
  def edit(): String = {
    var entity = getEntity(entityType, shortName)
    put(shortName, entity)
    editSetting(entity)
    return forward()
  }

  /**
   * Remove entities by [entity.id]/entityIds
   */
  def remove(): View = {
    val idclass = entityMetaData.getType(entityName).get.idType
    val entityId = getId(shortName, idclass)
    val entities =
      if (null == entityId) getModels[Object](entityName, getIds(shortName, idclass))
      else List(getModel[Object](entityName, entityId))
    removeAndRedirect(entities)
  }

  /**
   * Save single entity
   */
  def save(): View = {
    saveAndRedirect(populateEntity())
  }

  /**
   * 查看信息
   */
  def info(): String = {
    val entityId = getId(shortName, entityMetaData.getType(entityName).get.idType)
    if (null != entityId) {
      val entity: Entity[_] = getModel(entityName, entityId)
      put(shortName, entity)
    }
    forward()
  }

  @ignore
  protected def indexSetting(): Unit = {}

  @ignore
  protected def editSetting(entity: T): Unit = {}

}