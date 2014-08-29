package org.beangle.webmvc.api.action

import org.beangle.webmvc.api.context.Params
import org.beangle.commons.lang.Strings
import scala.reflect.ClassTag

trait EntityActionSupport extends ActionSupport {

  def getEntityType: Class[_]

  protected def getId(name: String): String = {
    get(name + ".id").getOrElse(get(name + "Id").getOrElse(get("id").getOrElse(null)))
  }
  /**
   * Get entity's id from shortname.id,shortnameId,id
   */
  protected final def getId[T](name: String, clazz: Class[T]): T = {
    val entityId = getId(name)
    if (entityId == null) null.asInstanceOf[T]
    else Params.converter.convert(entityId, clazz)
  }

  protected final def getIntId(shortName: String): Int = getId(shortName, classOf[Int])

  protected final def getLongId(shortName: String): Long = getId(shortName, classOf[Long])

  /**
   * Get entity's long id array from parameters shortname.id,shortname.ids,shortnameIds
   */
  protected final def getLongIds(shortName: String): Array[Long] = getIds(shortName, classOf[Long])

  /**
   * Get entity's long id array from parameters shortname.id,shortname.ids,shortnameIds
   */
  protected final def getIntIds(shortName: String): Array[Int] = getIds(shortName, classOf[Int])

  /**
   * Get entity's id array from parameters shortname.id,shortname.ids,shortnameIds
   */
  protected final def getIds[T: ClassTag](name: String, clazz: Class[T]): Array[T] = {
    val datas = Params.getAll(name + ".id", clazz.asInstanceOf[Class[Any]])
    if (null == datas) {
      val datastring = Params.get(name + ".ids").getOrElse(Params.get(name + "Ids").getOrElse(null))
      if (datastring == null) new Array[T](0) else Params.converter.convert(Strings.split(datastring, ","), clazz)
    }
    datas.asInstanceOf[Array[T]]
  }

}