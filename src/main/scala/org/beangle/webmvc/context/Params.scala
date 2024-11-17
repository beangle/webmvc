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

package org.beangle.webmvc.context

import org.beangle.commons.collection.MapConverter
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Strings.join

import java.time.{Instant, LocalDate, LocalDateTime}
import scala.collection.Map
import scala.reflect.ClassTag

object Params {

  val converter: MapConverter = new MapConverter(DefaultConversion.Instance)

  def get(attr: String): Option[String] = {
    ActionContext.current.params.get(attr) match {
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
    converter.get(ActionContext.current.params, name, clazz)
  }

  def getAll(attr: String): Iterable[Any] = {
    ActionContext.current.params.get(attr) match {
      case Some(value) =>
        if (null == value) List.empty
        else {
          if (value.getClass.isArray) value.asInstanceOf[Array[Any]].toList
          else List(value)
        }
      case None => List.empty
    }
  }

  def getAll[T](attr: String, clazz: Class[T]): Iterable[T] = {
    val value = getAll(attr)
    if (value.isEmpty) {
      List.empty[T]
    } else {
      value.flatMap(x => converter.convert(x, clazz))
    }
  }

  def getBoolean(name: String): Option[Boolean] = {
    converter.getBoolean(ActionContext.current.params, name)
  }

  def getBoolean(name: String, defaultValue: Boolean): Boolean = {
    converter.getBoolean(ActionContext.current.params, name).getOrElse(defaultValue)
  }

  def getDate(name: String): Option[LocalDate] = {
    converter.getDate(ActionContext.current.params, name)
  }

  def getDateTime(name: String): Option[LocalDateTime] = {
    converter.getDateTime(ActionContext.current.params, name)
  }

  def getInstant(name: String): Option[Instant] = {
    converter.get[String](ActionContext.current.params, name, classOf[String]) match {
      case None => None
      case Some(v) => converter.convert(v, classOf[Instant])
    }
  }

  def getFloat(name: String): Option[Float] = {
    converter.getFloat(ActionContext.current.params, name)
  }

  def getShort(name: String): Option[Short] = {
    converter.getShort(ActionContext.current.params, name)
  }

  def getShort(name: String, defaultValue: Short): Short = {
    converter.getShort(ActionContext.current.params, name).getOrElse(defaultValue)
  }

  def getInt(name: String): Option[Int] = {
    converter.getInt(ActionContext.current.params, name)
  }

  def getInt(name: String, defaultValue: Int): Int = {
    converter.getInt(ActionContext.current.params, name).getOrElse(defaultValue)
  }

  def getLong(name: String): Option[Long] = {
    converter.getLong(ActionContext.current.params, name)
  }

  def getLong(name: String, defaultValue: Long): Long = {
    converter.getLong(ActionContext.current.params, name).getOrElse(defaultValue)
  }

  def getId(name: String): Option[String] = {
    var ids = Params.getAll(name + ".id")
    if ids.isEmpty then ids = Params.getAll(name + "Id")
    if ids.isEmpty then ids = Params.getAll(name + "_id")
    if ids.isEmpty then ids = Params.getAll("id")

    if ids.isEmpty || ids.size > 1 then None
    else
      val head = ids.head.toString
      if (Strings.isBlank(head)) None else Some(head)
  }

  def getId[E](name: String, clazz: Class[E]): Option[E] = {
    getId(name) match {
      case Some(id) => Params.converter.convert(id, clazz)
      case None => None
    }
  }

  def getIntId(shortName: String): Int = Params.getId(shortName, classOf[Int]).get

  def getLongId(shortName: String): Long = Params.getId(shortName, classOf[Long]).get

  def getIds(name: String): List[String] = {
    val datas = getAll(name + ".id").asInstanceOf[List[String]]
    if (datas.isEmpty) {
      get(name + ".ids").orElse(get(name + "Ids")) match {
        case None => List.empty[String]
        case Some(datastring) => Strings.split(datastring, ",").toList
      }
    } else datas
  }

  def getIds[X](name: String, clazz: Class[X]): List[X] = {
    val datas = getAll(name + ".id", clazz).asInstanceOf[List[X]]
    if (datas.isEmpty) {
      get(name + ".ids").orElse(get(name + "Ids")) match {
        case None => List.empty[X]
        case Some(datastring) => converter.convert(Strings.split(datastring, ","), clazz).toList
      }
    } else datas
  }

  def getLongIds(shortName: String): List[Long] = Params.getIds(shortName, classOf[Long])

  def getIntIds(shortName: String): List[Int] = Params.getIds(shortName, classOf[Int])

  def sub(prefix: String): Map[String, Any] = converter.sub(ActionContext.current.params, prefix)

  def sub(prefix: String, exclusiveAttrNames: String): Map[String, Any] = {
    converter.sub(ActionContext.current.params, prefix, exclusiveAttrNames)
  }
}
