package org.beangle.webmvc.entity.action

import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.action.EntityActionSupport
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.entity.helper.{ PopulateHelper, QueryHelper }

abstract class AbstractEntityAction extends EntityActionSupport {

  /**
   * 将request中的参数设置到clazz对应的bean。
   */
  protected final def populate[T <: Entity[_]](clazz: Class[T], shortName: String): T = PopulateHelper.populate(clazz, shortName);

  protected final def populate(obj: Entity[_], shortName: String) = PopulateHelper.populate(obj, Params.sub(shortName))

  protected final def populate[T <: Entity[_]](clazz: Class[T]) = PopulateHelper.populate(clazz);

  protected final def populate(entityName: String) = PopulateHelper.populate(entityName);

  protected final def populate(entityName: String, shortName: String) = PopulateHelper.populate(entityName, shortName);

  protected final def populate(obj: Entity[_], entityName: String, shortName: String) = PopulateHelper.populate(obj, entityName, shortName);

  protected final def populate(entity: Entity[_], entityName: String, params: Map[String, Object]): Unit = {
    require(null != entity, "Cannot populate to null.")
    PopulateHelper.populate(entity, entityName, params);
  }

  protected final def populate(entity: Entity[_], params: Map[String, Object]) {
    require(null != entity, "Cannot populate to null.")
    PopulateHelper.populate(entity, params);
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
  final def entityName: String = getEntityType.getName

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
}