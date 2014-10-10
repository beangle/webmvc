package org.beangle.webmvc.api.context

import scala.collection.Map
import scala.reflect.ClassTag

import org.beangle.commons.collection.MapConverter
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.lang.Strings.join

object Params {

  val converter: MapConverter = new MapConverter(DefaultConversion.Instance)

  def get(attr: String): Option[String] = {
    ContextHolder.context.params.get(attr) match {
      case Some(value) =>
        if (null == value) None
        else {
          if (value.getClass.isArray) {
            val values = value.asInstanceOf[Array[String]]
            if (values.length == 1) Some(values(0))
            else Some(join(values, ","))
          } else Some(value.toString)
        }
      case _ => None
    }
  }

  def get[T](name: String, clazz: Class[T]) = converter.get(ContextHolder.context.params, name, clazz)

  def getAll(attr: String): Array[Any] = {
    ContextHolder.context.params.get(attr) match {
      case Some(value) =>
        if (null == value) null
        if (value.getClass.isArray()) value.asInstanceOf[Array[Any]]
        else Array(value)
      case None => null
    }
  }

  def getAll[T: ClassTag](attr: String, clazz: Class[T]): Array[T] = {
    val value = getAll(attr)
    if (null == value) null
    else converter.convert(value.asInstanceOf[Array[AnyRef]], clazz)
  }

  def getBoolean(name: String) = converter.getBoolean(ContextHolder.context.params, name)

  def getDate(name: String) = converter.getDate(ContextHolder.context.params, name)

  def getDateTime(name: String) = converter.getDateTime(ContextHolder.context.params, name)

  def getFloat(name: String) = converter.getFloat(ContextHolder.context.params, name)

  def getShort(name: String) = converter.getShort(ContextHolder.context.params, name)

  def getInt(name: String) = converter.getInt(ContextHolder.context.params, name)

  def getLong(name: String) = converter.getLong(ContextHolder.context.params, name)

  def sub(prefix: String): Map[String, Any] = converter.sub(ContextHolder.context.params, prefix)

  def sub(prefix: String, exclusiveAttrNames: String): Map[String, Any] = {
    converter.sub(ContextHolder.context.params, prefix, exclusiveAttrNames)
  }
}