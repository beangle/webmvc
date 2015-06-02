package org.beangle.webmvc.api.action

import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.context.ContextHolder
import scala.reflect.ClassTag
import java.sql
import java.{ util => ju }

trait ParamSupport {

  protected final def put(key: String, value: Any) {
    ContextHolder.context.attribute(key, value)
  }

  protected final def getAll(paramName: String): Array[Any] = {
    Params.getAll(paramName)
  }

  protected final def getAll[T: ClassTag](paramName: String, clazz: Class[T]): Array[T] = {
    Params.getAll(paramName, clazz)
  }

  protected final def get(paramName: String): Option[String] = {
    Params.get(paramName)
  }

  protected final def get[T](paramName: String, defaultValue: T): T = {
    val value = Params.get(paramName)
    if (value.isEmpty) defaultValue else Params.converter.convert(value.get, defaultValue.getClass).getOrElse(defaultValue)
  }

  protected final def getAttribute(name: String): Any = {
    ContextHolder.context.attribute(name)
  }

  protected final def getAttribute[T](name: String, clazz: Class[T]): T = {
    ContextHolder.context.attribute(name).asInstanceOf[T]
  }

  protected final def get[T](name: String, clazz: Class[T]): Option[T] = {
    Params.get(name, clazz)
  }

  protected final def getBoolean(name: String): Option[Boolean] = {
    Params.getBoolean(name)
  }

  protected final def getBoolean(name: String, defaultValue: Boolean): Boolean = {
    Params.getBoolean(name).getOrElse(defaultValue)
  }

  protected final def getDate(name: String): Option[sql.Date] = {
    Params.getDate(name)
  }

  protected final def getDateTime(name: String): Option[ju.Date] = {
    Params.getDateTime(name)
  }

  protected final def getFloat(name: String): Option[Float] = {
    Params.getFloat(name)
  }

  protected final def getShort(name: String): Option[Short] = {
    Params.getShort(name)
  }

  protected final def getInt(name: String): Option[Int] = {
    Params.getInt(name)
  }

  protected final def getInt(name: String, defaultValue: Int): Int = {
    Params.getInt(name).getOrElse(defaultValue)
  }

  protected final def getLong(name: String): Option[Long] = {
    Params.getLong(name)
  }
}