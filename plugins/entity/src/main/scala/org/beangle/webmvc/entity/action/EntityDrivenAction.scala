package org.beangle.webmvc.entity.action

import org.beangle.commons.lang.Strings
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.view.View
import java.{ io => jo }

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

  def getExportDatas[T <: Entity[_]](): Seq[T] = {
    // 自动会考虑页面是否传入id
    val result: Option[Seq[T]] = if (Strings.isNotBlank(entityName)) {
      entityMetaData.getType(entityName) match {
        case Some(entityType) => {
          val ids = getIds(shortName, entityType.idType)
          if (ids != null && ids.length > 0) {
            val rs = entityDao.find(entityType.entityClass.asInstanceOf[Class[Entity[jo.Serializable]]], ids.toList)
            Some(rs.asInstanceOf[Seq[T]])
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
    val entities: Seq[T] =
      if (null == entityId) getModels(entityName, getIds(shortName, idclass))
      else List(getModel(entityName, entityId))
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