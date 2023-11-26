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

package org.beangle.webmvc.support.helper

import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.{Page, PageLimit}
import org.beangle.commons.lang.reflect.{BeanInfos, Reflections}
import org.beangle.commons.lang.{Numbers, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.data.dao.{Condition, Conditions, OqlBuilder}
import org.beangle.data.model.Entity
import org.beangle.data.model.meta.SingularProperty
import org.beangle.web.action.context.{ActionContext, Params}
import org.beangle.web.servlet.util.CookieUtils

import java.net.URLEncoder
import java.text.{ParseException, SimpleDateFormat}
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import java.util as ju
import scala.collection.mutable

object QueryHelper extends Logging {

  val PageParam = "pageIndex"

  val PageSizeParam = "pageSize"

  val RESERVED_NULL = true

  private val keySeparators = Set(',', '，', ';', '；')

  @deprecated
  def populateConditions(builder: OqlBuilder[_]): this.type = {
    builder.where(extractConditions(builder.entityClass, builder.alias, null))
    this
  }

  def populate(entityQuery: OqlBuilder[_], exclusiveAttrNames: String): this.type = {
    entityQuery.where(extractConditions(entityQuery.entityClass, entityQuery.alias,
      exclusiveAttrNames))
    this
  }

  /**
   * 提取中的条件
   *
   * @param clazz              实体类型
   * @param prefix             参数中的前缀（不包含最后的.）
   * @param exclusiveAttrNames 排除属性列表(prefix.attr1,prefix.attr2)
   */
  def extractConditions(clazz: Class[_], prefix: String, exclusiveAttrNames: String): List[Condition] = {
    var entity: Entity[_] = null
    var newClazz: Class[_] = clazz
    val entityType = PopulateHelper.getType(clazz)
    try {
      if (clazz.isInterface) newClazz = entityType.clazz
      entity = Reflections.newInstance(clazz.asInstanceOf[Class[Entity[_]]])
    } catch {
      case _: Exception => throw new RuntimeException("[RequestUtil.extractConditions]: error in in initialize " + clazz)
    }
    val conditions = new mutable.ListBuffer[Condition]
    val params = Params.sub(prefix, exclusiveAttrNames)
    val paramIter = params.iterator
    while (paramIter.hasNext) {
      val entry = paramIter.next()
      val attr = entry._1
      val value = entry._2
      if (null != value) {
        val strValue = if value.getClass.isArray then value.asInstanceOf[Array[Any]].mkString(",") else value.toString
        if (Strings.isNotBlank(strValue)) {
          if (attr.endsWith("}")) {
            val attrs = splitAttrs(attr)
            val locals = new mutable.ListBuffer[Condition]
            attrs.foreach { attr1 =>
              val vt = PopulateHelper.populator.init(entity, entityType, attr1)
              if null != vt && vt._2.isInstanceOf[SingularProperty] then
                locals += Conditions.parse(s"$prefix.$attr1", strValue, vt._2.clazz)
            }
            if locals.size == 1 then
              conditions += locals.head
            else if locals.size > 1 then
              val orContent = locals.map(x => x.content).mkString(" or ")
              val orCond = new Condition(orContent)
              orCond.params ++= locals.flatMap(x => x.params)
              conditions += orCond
          } else {
            val vt = PopulateHelper.populator.init(entity, entityType, attr)
            if null != vt && vt._2.isInstanceOf[SingularProperty] then
              conditions += Conditions.parse(s"$prefix.$attr", strValue, vt._2.clazz)
          }
        }
      }
    }
    conditions.toList
  }

  private def splitAttrs(attr: String): Seq[String] = {
    if attr.endsWith("}") then
      val head = Strings.substringBefore(attr, "{")
      if Strings.isEmpty(head) && attr.charAt(0) != '{' then
        List(attr)
      else
        val subAttrs = Strings.split(Strings.substringBetween(attr, "{", "}"), ',')
        subAttrs.map(x => head + x).toSeq
    else
      List(attr)
  }

  private def getAll(params: collection.Map[String, Any], attr: String): List[Any] = {
    params.get(attr) match {
      case Some(value) =>
        if (null == value) List.empty
        else {
          if (value.getClass.isArray) value.asInstanceOf[Array[Any]].toList
          else List(value)
        }
      case None => List.empty
    }
  }

  private def getAll[T](params: collection.Map[String, Any], attr: String, clazz: Class[T]): List[T] = {
    val value = getAll(params, attr)
    if (value.isEmpty) {
      List.empty[T]
    } else {
      value.flatMap(x => Params.converter.convert(x, clazz))
    }
  }

  /**
   * 把entity alias的别名的参数转换成条件.<br>
   *
   * @param entityQuery        查询构建器
   * @param exclusiveAttrNames 以entityQuery中alias开头的属性串
   */
  def populate(builder: OqlBuilder[_]): this.type = {
    builder.where(extractConditions(builder.entityClass, builder.alias, null))
    this
  }

  /**
   * 从的参数或者cookie中(参数优先)取得分页信息
   */
  def pageLimit: PageLimit = {
    new PageLimit(pageIndex, pageSize)
  }

  /**
   * 获得请求中的页码
   */
  def pageIndex: Int = {
    val pageIndex = Params.getInt(PageParam) match {
      case Some(p) => p
      case None => Params.getInt("page[number]").getOrElse(Page.DefaultPageNo)
    }
    if (pageIndex < 1) Page.DefaultPageNo else pageIndex
  }

  /**
   * 获得请求中的页长
   */
  def pageSize: Int = {
    var pageSize = Params.get(PageSizeParam).orElse(Params.get("page[size]")).getOrElse("")
    var pagesize = Page.DefaultPageSize
    if (Strings.isNotBlank(pageSize)) {
      pagesize = Numbers.toInt(pageSize.trim())
    } else {
      pageSize = CookieUtils.getCookieValue(ActionContext.current.request, PageSizeParam)
      if (Strings.isNotEmpty(pageSize)) pagesize = Numbers.toInt(pageSize)
    }
    if (pagesize < 1) Page.DefaultPageSize else pagesize
  }

  def sort(query: OqlBuilder[_]): this.type = {
    val sort = Params.get(Order.OrderStr) match {
      case orderBy@Some(_) => orderBy
      case None => Params.get("sort")
    }
    sort foreach { orderClause =>
      query.orderBy(orderClause)
    }
    this
  }

  def limit(query: OqlBuilder[_]): this.type = {
    query.limit(pageIndex, pageSize)
    this
  }

  @deprecated("Using dateBetween")
  def addDateIntervalCondition(query: OqlBuilder[_], attr: String, beginOnName: String, endOnName: String): Unit = {
    dateBetween(query, query.alias, attr, beginOnName, endOnName)
  }

  @deprecated("Using dateBetween")
  def addDateIntervalCondition(query: OqlBuilder[_], alias: String, attr: String, beginOnName: String,
                               endOnName: String): Unit = {
    dateBetween(query, alias, attr, beginOnName, endOnName)
  }

  /**
   * 增加日期区间查询条件
   *
   * @param query       查询构建器
   * @param alias       别名
   * @param attr        时间限制属性
   * @param beginOnName 开始的属性名字(全名)
   * @param endOnName   结束的属性名字(全名)
   */
  def dateBetween(query: OqlBuilder[_], alias: String, attr: String, beginOnName: String,
                  endOnName: String): Unit = {
    val stime = Params.get(beginOnName)
    val etime = Params.get(endOnName)
    val df = new SimpleDateFormat("yyyy-MM-dd")
    val sdate =
      if (stime.isDefined) try {
        Some(df.parse(stime.get))
      } catch {
        case _: ParseException => None
      } else None

    var edate =
      if (etime.isDefined) try {
        Some(df.parse(etime.get))
      } catch {
        case _: ParseException => None
      } else None

    // 截至日期增加一天
    if (edate.isDefined) {
      val gc = new ju.GregorianCalendar()
      gc.setTime(edate.get)
      gc.set(ju.Calendar.DAY_OF_YEAR, gc.get(ju.Calendar.DAY_OF_YEAR) + 1)
      edate = Some(gc.getTime)
    }
    val objAttr = (if (null == alias) query.alias else alias) + "." + attr

    if (null != query.entityClass) {
      BeanInfos.get(query.entityClass).getPropertyType(attr) match {
        case Some(pc) =>
          if (classOf[LocalDateTime].isAssignableFrom(pc)) {
            val start = sdate.map(x => LocalDateTime.ofInstant(x.toInstant, ZoneId.systemDefault()))
            val end = edate.map(x => LocalDateTime.ofInstant(x.toInstant, ZoneId.systemDefault()))
            between(query, objAttr, start, end)
          } else if (classOf[Instant].isAssignableFrom(pc)) {
            between(query, objAttr, sdate.map(_.toInstant), edate.map(_.toInstant))
          } else if (classOf[LocalDate].isAssignableFrom(pc)) {
            val start = sdate.map(x => LocalDate.ofInstant(x.toInstant, ZoneId.systemDefault()))
            val end = edate.map(x => LocalDate.ofInstant(x.toInstant, ZoneId.systemDefault()))
            between(query, objAttr, start, end)
          } else {
            between(query, objAttr, sdate, edate)
          }
        case None =>
          between(query, objAttr, sdate, edate)
      }
    }
  }

  private def between(query: OqlBuilder[_], path: String, sdate: Option[AnyRef], edate: Option[AnyRef]): Unit = {
    if (sdate.isDefined && edate.isEmpty) {
      query.where(path + " >=:sdate", sdate.get)
    } else if (sdate.isDefined && edate.isDefined) {
      query.where(path + " >=:sdate and " + path + " <:edate", sdate.get, edate.get)
    } else if (sdate.isEmpty && edate.isDefined) {
      query.where(path + " <:edate", edate.get)
    }
  }
}
