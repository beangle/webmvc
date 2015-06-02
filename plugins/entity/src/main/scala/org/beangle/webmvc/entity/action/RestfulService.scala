package org.beangle.webmvc.entity.action

import org.beangle.data.model.Entity
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{ mapping, param, response }
import org.beangle.webmvc.api.context.Params

class RestfulService[T <: Entity[_ <: java.io.Serializable]] extends ActionSupport with EntityAction[T] {

  @response
  def index(): Any = {
    getInt("page") match {
      case Some(p) => entityDao.search(getQueryBuilder())
      case None => entityDao.search(getQueryBuilder().limit(null))
    }
  }

  @response
  @mapping(value = "{id}")
  def info(@param("id") id: String): T = {
    Params.converter.convert(id, entityMetaData.getType(entityName).get.idType) match {
      case None => null.asInstanceOf[T]
      case Some(entityId) => getModel[T](entityName, entityId)
    }
  }

}