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
package org.beangle.webmvc.api.context

import java.{ sql, util => ju }

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

  def get[T](name: String, clazz: Class[T]): Option[T] = {
    converter.get(ContextHolder.context.params, name, clazz)
  }

  def getAll(attr: String): Iterable[Any] = {
    ContextHolder.context.params.get(attr) match {
      case Some(value) =>
        if (null == value) List.empty
        else {
          if (value.getClass.isArray) value.asInstanceOf[Array[Any]].toList
          else List(value)
        }
      case None => List.empty
    }
  }

  def getAll[T: ClassTag](attr: String, clazz: Class[T]): Iterable[T] = {
    val value = getAll(attr)
    if (value.isEmpty) List.empty[T]
    else value.map(x => converter.convert(x, clazz).get)
  }

  def getBoolean(name: String): Option[Boolean] = {
    converter.getBoolean(ContextHolder.context.params, name)
  }

  def getDate(name: String): Option[sql.Date] = {
    converter.getDate(ContextHolder.context.params, name)
  }

  def getDateTime(name: String): Option[ju.Date] = {
    converter.getDateTime(ContextHolder.context.params, name)
  }

  def getFloat(name: String): Option[Float] = {
    converter.getFloat(ContextHolder.context.params, name)
  }

  def getShort(name: String): Option[Short] = {
    converter.getShort(ContextHolder.context.params, name)
  }

  def getInt(name: String): Option[Int] = {
    converter.getInt(ContextHolder.context.params, name)
  }

  def getLong(name: String): Option[Long] = {
    converter.getLong(ContextHolder.context.params, name)
  }

  def sub(prefix: String): Map[String, Any] = converter.sub(ContextHolder.context.params, prefix)

  def sub(prefix: String, exclusiveAttrNames: String): Map[String, Any] = {
    converter.sub(ContextHolder.context.params, prefix, exclusiveAttrNames)
  }
}