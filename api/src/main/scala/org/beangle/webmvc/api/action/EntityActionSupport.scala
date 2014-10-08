package org.beangle.webmvc.api.action

import scala.reflect.ClassTag

import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.webmvc.api.context.Params

trait EntityActionSupport[T] extends ActionSupport {

  val entityType: Class[T] = {
    val tClass = Reflections.getGenericParamType(getClass, classOf[EntityActionSupport[_]]).get("T")
    if (tClass.isEmpty) throw new RuntimeException(s"Cannot guess entity type from ${this.getClass.getName}")
    else tClass.get.asInstanceOf[Class[T]]
  }

  protected def getId(name: String): String = {
    get(name + ".id").getOrElse(get(name + "_id").getOrElse(get(name + "Id").getOrElse(null)))
  }
  /**
   * Get entity's id from shortname.id,shortnameId,id
   */
  protected final def getId[E](name: String, clazz: Class[E]): E = {
    val entityId = getId(name)
    if (entityId == null) null.asInstanceOf[E]
    else Params.converter.convert(entityId, clazz)
  }

  protected final def getIntId(shortName: String): java.lang.Integer = getId(shortName, classOf[java.lang.Integer])

  protected final def getLongId(shortName: String): java.lang.Long = getId(shortName, classOf[java.lang.Long])

  /**
   * Get entity's long id array from parameters shortname.id,shortname.ids,shortnameIds
   */
  protected final def getLongIds(shortName: String): Array[java.lang.Long] = getIds(shortName, classOf[java.lang.Long])

  /**
   * Get entity's long id array from parameters shortname.id,shortname.ids,shortnameIds
   */
  protected final def getIntIds(shortName: String): Array[java.lang.Integer] = getIds(shortName, classOf[java.lang.Integer])

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