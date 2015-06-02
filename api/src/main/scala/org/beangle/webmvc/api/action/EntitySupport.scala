package org.beangle.webmvc.api.action

import scala.reflect.ClassTag

import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.webmvc.api.context.Params

trait EntitySupport[T] {

  val entityType: Class[T] = {
    val tClass = Reflections.getGenericParamType(getClass, classOf[EntitySupport[_]]).get("T")
    if (tClass.isEmpty) throw new RuntimeException(s"Cannot guess entity type from ${this.getClass.getName}")
    else tClass.get.asInstanceOf[Class[T]]
  }

  protected def getId(name: String): String = {
    Params.get(name + ".id").getOrElse(Params.get(name + "_id").getOrElse(Params.get(name + "Id").getOrElse(null)))
  }
  /**
   * Get entity's id from shortname.id,shortnameId,id
   */
  protected final def getId[E](name: String, clazz: Class[E]): E = {
    val entityId = getId(name)
    if (entityId == null) null.asInstanceOf[E]
    else Params.converter.convert(entityId, clazz).getOrElse(null.asInstanceOf[E])
  }

  protected final def getIntId(shortName: String): java.lang.Integer = getId(shortName, classOf[java.lang.Integer])

  protected final def getLongId(shortName: String): java.lang.Long = getId(shortName, classOf[java.lang.Long])

  /**
   * Get entity's long id array from parameters shortname.id,shortname.ids,shortnameIds
   */
  protected final def getLongIds(shortName: String): Array[java.lang.Long] = {
    getIds(shortName, classOf[java.lang.Long])
  }

  /**
   * Get entity's long id array from parameters shortname.id,shortname.ids,shortnameIds
   */
  protected final def getIntIds(shortName: String): Array[java.lang.Integer] = {
    getIds(shortName, classOf[java.lang.Integer])
  }

  /**
   * Get entity's id array from parameters shortname.id,shortname.ids,shortnameIds
   */
  protected final def getIds[T: ClassTag](name: String, clazz: Class[T]): Array[T] = {
    var datas: Any = Params.getAll(name + ".id", clazz.asInstanceOf[Class[Any]])
    if (null == datas) {
      val datastring = Params.get(name + ".ids").getOrElse(Params.get(name + "Ids").getOrElse(null))
      datas = if (datastring == null) java.lang.reflect.Array.newInstance(clazz, 0)
      else Params.converter.convert(Strings.split(datastring, ","), clazz)
    }
    datas.asInstanceOf[Array[T]]
  }

}