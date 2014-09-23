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

abstract class EntityDrivenAction[T <: Entity[T]] extends AbstractEntityAction[T] {

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
    var entity = getEntity(getEntityType, shortName)
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
    removeAndForward(entities)
  }

  /**
   * Save single entity
   */
  def save(): View = {
    saveAndForward(populateEntity())
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

  /**
   * 保存对象
   *
   * @param entity
   */
  @ignore
  protected def saveAndForward(entity: T): View = {
    try {
      if (entity.isInstanceOf[UpdatedBean]) {
        val timeEntity = entity.asInstanceOf[UpdatedBean]
        timeEntity.updatedAt = new ju.Date()
      }
      saveOrUpdate(entity)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
      }
    }
  }

  @ignore
  protected def removeAndForward(entities: Seq[_]): View = {
    try {
      remove(entities)
      redirect("search", "info.remove.success")
    } catch {
      case e: Exception => {
        info("removeAndForwad failure", e)
        redirect("search", "info.delete.failure")
      }
    }
  }
}