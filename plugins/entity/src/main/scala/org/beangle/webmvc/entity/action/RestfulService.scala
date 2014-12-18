package org.beangle.webmvc.entity.action

import org.beangle.data.model.Entity
import org.beangle.webmvc.api.annotation.{ mapping, param }
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.annotation.response
import org.beangle.commons.http.accept.ContentNegotiationManager

class RestfulService[T <: Entity[_ <: java.io.Serializable]] extends AbstractEntityAction[T] {

  var contentNegotiationManager: ContentNegotiationManager = _

  def isRequestCsv: Boolean = {
    if (null == contentNegotiationManager) false
    else {
      contentNegotiationManager.resolve(request).exists { p => p.getBaseType == "text/csv" }
    }
  }

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