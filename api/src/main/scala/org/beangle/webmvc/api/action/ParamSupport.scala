/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.api.action

import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.context.ActionContext
import scala.reflect.ClassTag
import java.sql
import java.{ util => ju }

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