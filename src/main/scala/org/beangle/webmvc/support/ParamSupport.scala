/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.support

import org.beangle.webmvc.context.{ActionContext, Params}

import java.time.{Instant, LocalDate, LocalDateTime}
import scala.reflect.ClassTag

trait ParamSupport {

  protected final def put(key: String, value: Any): Unit = {
    ActionContext.current.attribute(key, value)
  }

  protected final def getAll(paramName: String): Iterable[Any] = {
    Params.getAll(paramName)
  }

  protected final def getAll[T: ClassTag](paramName: String, clazz: Class[T]): Iterable[T] = {
    Params.getAll(paramName, clazz)
  }

  protected final def get(paramName: String): Option[String] = {
    Params.get(paramName)
  }

  protected final def get[T](paramName: String, defaultValue: T): T = {
    val value = Params.get(paramName)
    if (value.isEmpty) defaultValue else Params.converter.convert(value.get, defaultValue.getClass).getOrElse(defaultValue)
  }

  protected final def attribute(name: String): Any = {
    ActionContext.current.attribute(name)
  }

  protected final def attribute[T](name: String, clazz: Class[T]): T = {
    ActionContext.current.attribute(name).asInstanceOf[T]
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

  protected final def getDate(name: String): Option[LocalDate] = {
    Params.getDate(name)
  }

  protected final def getDateTime(name: String): Option[LocalDateTime] = {
    Params.getDateTime(name)
  }

  protected final def getInstant(name: String): Option[Instant] = {
    Params.getInstant(name)
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

  def getId(name: String): Option[String] = Params.getId(name)

  def getId[E](name: String, clazz: Class[E]): Option[E] = Params.getId(name, clazz)

  def getIntId(shortName: String): Int = Params.getIntId(shortName)

  def getLongId(shortName: String): Long = Params.getLongId(shortName)

  def getIds(name: String): List[String] = Params.getIds(name)

  def getIds[X](name: String, clazz: Class[X]): List[X] = Params.getIds(name, clazz)

  def getLongIds(shortName: String): List[Long] = Params.getLongIds(shortName)

  def getIntIds(shortName: String): List[Int] = Params.getIntIds(shortName)

}
