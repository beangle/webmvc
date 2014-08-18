package org.beangle.webmvc.action

import java.{ util => ju }
import org.beangle.commons.collection.Order
import org.beangle.commons.config.property.PropertyConfig
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.data.model.bean.UpdatedBean
import org.beangle.data.model.dao.{ GeneralDao, QueryBuilder }
import org.beangle.data.model.meta.{ EntityMetadata, EntityType }
import org.beangle.webmvc.api.context.Params

abstract class EntityDrivenAction extends EntityActionSupport {

  var entityDao: GeneralDao = _
  var config: PropertyConfig = _
  var entityMetaData: EntityMetadata = _
  // CURD----------------------------------------
  protected def remove[T](list: Seq[T]): Unit = {
    entityDao.remove(list)
  }

  protected def remove[T](obj: T): Unit = {
    entityDao.remove(obj)
  }

  protected def saveOrUpdate[T](list: Iterable[T]): Unit = {
    entityDao.saveOrUpdate(list)
  }

  protected def saveOrUpdate[T](obj: T): Unit = {
    entityDao.saveOrUpdate(obj)
  }

  protected def search[T](query: QueryBuilder[T]): Seq[T] = {
    entityDao.search(query)
  }

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
    put(shortName + "s", search(getQueryBuilder()))
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
    result.getOrElse(search(getQueryBuilder().limit(null)).asInstanceOf)
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
  def remove(): String = {
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
  def save(): String = {
    saveAndForward(populateEntity())
  }

  protected def populateEntity(): Entity[_] = {
    populateEntity(entityName, shortName)
  }

  protected def populateEntity(entityName: String, shortName: String): Entity[_] = {
    val entityId: Serializable = getId(shortName, entityMetaData.getType(entityName).get.idClass)
    if (null == entityId) {
      populate(entityName, shortName).asInstanceOf[Entity[_]]
    } else {
      val entity: Entity[_] = getModel(entityName, entityId)
      populate(entity, entityName, Params.sub(shortName).asInstanceOf[Map[String, Object]])
      entity.asInstanceOf[Entity[_]]
    }
  }

  protected def populateEntity[T](entityClass: Class[T], shortName: String): T = {
    val entityType: EntityType = (if (entityClass.isInterface) {
      entityMetaData.getType(entityClass.getName)
    } else {
      entityMetaData.getType(entityClass)
    }).get
    populateEntity(entityType.entityName, shortName).asInstanceOf[T]
  }

  protected def getEntity[T]: Entity[T] = {
    getEntity(entityName, shortName)
  }

  protected def getEntity[T](entityName: String, name: String): Entity[T] = {
    val entityType: EntityType = entityMetaData.getType(entityName).get
    val entityId: Serializable = getId(name, entityType.idClass)
    if (null == entityId)
      populate(entityType.newInstance.asInstanceOf[Entity[_]], entityType.entityName, name).asInstanceOf[Entity[T]]
    else getModel(entityName, entityId)
  }

  protected def getEntity[T](entityClass: Class[T], shortName: String): T = {
    val entityType: EntityType = (if (entityClass.isInterface)
      entityMetaData.getType(entityClass.getName)
    else entityMetaData.getType(entityClass)).get
    getEntity(entityType.entityName, shortName).asInstanceOf[T]
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
  protected def saveAndForward(entity: Entity[_]): String = {
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

  protected def removeAndForward(entities: Seq[_]): String = {
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

  protected def getQueryBuilder[T](): OqlBuilder[T] = {
    val builder: OqlBuilder[T] = OqlBuilder.from(entityName, shortName)
    populateConditions(builder)
    builder.orderBy(get(Order.OrderStr).get).limit(getPageLimit())
  }

  protected def getModel[T](entityName: String, id: Serializable): Entity[T] = {
    entityDao.get(Class.forName(entityName).asInstanceOf, id)
  }

  protected def getModels(entityName: String, ids: Array[_]): List[_] = {
    entityDao.find(Class.forName(entityName).asInstanceOf, "id", ids).asInstanceOf[List[_]]
  }
}