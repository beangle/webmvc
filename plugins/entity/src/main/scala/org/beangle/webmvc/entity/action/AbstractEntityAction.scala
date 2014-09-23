package org.beangle.webmvc.entity.action

import java.{ util => ju, io => jo }

import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.config.property.PropertyConfig
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.data.model.bean.UpdatedBean
import org.beangle.data.model.dao.GeneralDao
import org.beangle.data.model.meta.{ EntityMetadata, EntityType }
import org.beangle.webmvc.api.action.EntityActionSupport
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.helper.{ PopulateHelper, QueryHelper }

abstract class AbstractEntityAction[T <: Entity[_]] extends EntityActionSupport[T] {
  var entityDao: GeneralDao = _
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

  protected final def populate[E <: Entity[_]](clazz: Class[E]) = {
    PopulateHelper.populate(clazz);
  }

  protected final def populate(entityName: String) = PopulateHelper.populate(entityName);

  protected final def populate(entityName: String, shortName: String) = PopulateHelper.populate(entityName, shortName);

  protected final def populate[E <: Entity[_]](obj: E, entityName: String, shortName: String) = {
    PopulateHelper.populate(obj, entityName, shortName)
  }

  protected final def populate[E <: Entity[_]](entity: E, entityName: String, params: Map[String, Object]): E = {
    require(null != entity, "Cannot populate to null.")
    PopulateHelper.populate(entity, entityName, params)
  }

  protected final def populate[E <: Entity[_]](entity: E, params: Map[String, Object]): E = {
    require(null != entity, "Cannot populate to null.")
    PopulateHelper.populate(entity, params)
  }

  // query------------------------------------------------------
  protected final def getPageNo(): Int = QueryHelper.getPageNo();

  protected final def getPageSize(): Int = QueryHelper.getPageSize();

  /**
   * 从request的参数或者cookie中(参数优先)取得分页信息
   */
  protected final def getPageLimit(): PageLimit = QueryHelper.getPageLimit();

  protected final def populateConditions(builder: OqlBuilder[_]) {
    QueryHelper.populateConditions(builder);
  }

  protected final def populateConditions(builder: OqlBuilder[_], exclusiveAttrNames: String) {
    QueryHelper.populateConditions(builder, exclusiveAttrNames);
  }

  @ignore
  final def entityName: String = entityType.getName

  @ignore
  protected def shortName: String = {
    val name = entityName
    if (Strings.isNotEmpty(name)) getCommandName(name)
    else null
  }

  /**
   * replace EntityUtils.getCommandName(name)
   */
  private def getCommandName(entityName: String): String = {
    Strings.uncapitalize(Strings.substringAfterLast(entityName, "."))
  }

  // CURD----------------------------------------
  @ignore
  protected def remove[E](list: Seq[E]): Unit = {
    entityDao.remove(list)
  }

  @ignore
  protected def remove[E](obj: E): Unit = {
    entityDao.remove(obj)
  }

  @ignore
  protected def saveOrUpdate[E](list: Iterable[E]): Unit = {
    entityDao.saveOrUpdate(list)
  }

  @ignore
  protected def saveOrUpdate[E](obj: E): Unit = {
    entityDao.saveOrUpdate(obj)
  }

  protected def getQueryBuilder(): OqlBuilder[T] = {
    val builder: OqlBuilder[T] = OqlBuilder.from(entityName, shortName)
    populateConditions(builder)
    builder.orderBy(get(Order.OrderStr).orNull).limit(getPageLimit())
  }

  @ignore
  protected def populateEntity(): T = {
    populateEntity(entityName, shortName).asInstanceOf[T]
  }

  @ignore
  protected def populateEntity[E <: Entity[_]](entityName: String, shortName: String): E = {
    val entityId: jo.Serializable = getId(shortName, entityMetaData.getType(entityName).get.idType)
    if (null == entityId) {
      populate(entityName, shortName).asInstanceOf[E]
    } else {
      populate(getModel[E](entityName, entityId), entityName, Params.sub(shortName).asInstanceOf[Map[String, Object]])
    }
  }

  @ignore
  protected def populateEntity[E](entityClass: Class[E], shortName: String): E = {
    val entityType: EntityType =
      (if (entityClass.isInterface) {
        entityMetaData.getType(entityClass.getName)
      } else {
        entityMetaData.getType(entityClass)
      }).get
    populateEntity(entityType.entityName, shortName)
  }

  protected def getEntity[E <: Entity[_]](entityName: String, name: String): E = {
    val entityType: EntityType = entityMetaData.getType(entityName).get
    val entityId: jo.Serializable = getId(name, entityType.idType)
    if (null == entityId) populate(entityType.newInstance.asInstanceOf[E], entityType.entityName, name)
    else getModel(entityName, entityId).asInstanceOf[E]
  }

  protected def getEntity[E](entityClass: Class[E], shortName: String): E = {
    val entityType: EntityType =
      (if (entityClass.isInterface) entityMetaData.getType(entityClass.getName)
      else entityMetaData.getType(entityClass)).get
    getEntity(entityType.entityName, shortName).asInstanceOf[E]
  }

  protected def getModel(id: jo.Serializable): T = {
    val entityType: EntityType = entityMetaData.getType(entityName).get
    entityDao.get(entityType.entityClass.asInstanceOf[Class[T]], Params.converter.convert(id, entityType.idType))
  }

  protected def getModel[E](entityName: String, id: jo.Serializable): E = {
    val entityType: EntityType = entityMetaData.getType(entityName).get
    entityDao.get(entityType.entityClass.asInstanceOf[Class[E]], Params.converter.convert(id, entityType.idType))
  }

  protected def getModels[E](entityName: String, ids: Array[_]): List[E] = {
    entityDao.find(Class.forName(entityName).asInstanceOf[Class[E]], "id", ids).asInstanceOf[List[E]]
  }

  @ignore
  protected def saveAndRedirect(entity: T): View = {
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
  protected def removeAndRedirect(entities: Seq[_]): View = {
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