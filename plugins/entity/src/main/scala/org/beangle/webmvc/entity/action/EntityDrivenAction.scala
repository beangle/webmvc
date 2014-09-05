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

abstract class EntityDrivenAction extends AbstractEntityAction {

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
          val ids = getIds(shortName, entityType.idClass).asInstanceOf[Array[Serializable]]
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
    var entity = getEntity
    put(shortName, entity)
    editSetting(entity)
    return forward()
  }

  /**
   * Remove entities by [entity.id]/entityIds
   */
  def remove(): View = {
    val idclass = entityMetaData.getType(entityName).get.idClass.asInstanceOf[Class[Serializable]]
    val entityId: Serializable = getId(shortName, idclass)
    val entities: Seq[_] = if (null == entityId) {
      getModels(entityName, getIds(shortName, idclass))
    } else {
      val entity: Entity[_] = getModel(entityName, entityId)
      List(entity)
    }
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
    val entityId: Serializable = getId(shortName, entityMetaData.getType(entityName).get.idClass)
    if (null != entityId) {
      val entity: Entity[_] = getModel(entityName, entityId)
      put(shortName, entity)
    }
    forward()
  }

  protected def indexSetting(): Unit = {}

  protected def editSetting(entity: Entity[_]): Unit = {}

  /**
   * 保存对象
   *
   * @param entity
   */
  protected def saveAndForward(entity: Entity[_]): View = {
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