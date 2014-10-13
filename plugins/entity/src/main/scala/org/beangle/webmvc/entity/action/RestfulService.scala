package org.beangle.webmvc.entity.action

import org.beangle.data.model.Entity
import org.beangle.webmvc.api.annotation.{ mapping, param }
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.annotation.response

class RestfulService[T <: Entity[_ <: java.io.Serializable]] extends AbstractEntityAction[T] {

  @response
  def index(): Seq[T] = {
    getInt("page") match {
      case Some(p) => entityDao.search(getQueryBuilder())
      case None => entityDao.search(getQueryBuilder().limit(null))
    }
  }

  @response
  @mapping(value = "{id}")
  def info(@param("id") id: String): T = {
    val entityId = Params.converter.convert(id, entityMetaData.getType(entityName).get.idType)
    getModel[T](entityName, entityId)
  }

}