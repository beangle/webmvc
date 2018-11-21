/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.entity.helper

import java.text.{ ParseException, SimpleDateFormat }
import java.{ util => ju }

import org.beangle.commons.bean.Properties
import org.beangle.commons.collection.page.{ Page, PageLimit }
import org.beangle.commons.lang.{ Numbers, Strings }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.CookieUtils
import org.beangle.data.dao.{ Condition, OqlBuilder }
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.context.{ ActionContext, Params }

object QueryHelper extends Logging {

  val PageParam = "pageIndex"

  val PageSizeParam = "pageSize"

  val RESERVED_NULL = true

  def populateConditions(builder: OqlBuilder[_]) {
    builder.where(extractConditions(builder.entityClass, builder.alias, null))
  }

  /**
   * 把entity alias的别名的参数转换成条件.<br>
   *
   * @param entityQuery
   * @param exclusiveAttrNames   以entityQuery中alias开头的属性串
   */
  def populateConditions(entityQuery: OqlBuilder[_], exclusiveAttrNames: String) {
    entityQuery.where(extractConditions(entityQuery.entityClass, entityQuery.alias,
      exclusiveAttrNames))
  }

  /**
   * 提取中的条件
   *
   * @param clazz
   * @param prefix
   * @param exclusiveAttrNames
   */
  def extractConditions(clazz: Class[_], prefix: String, exclusiveAttrNames: String): List[Condition] = {
    var entity: Entity[_] = null
    var newClazz: Class[_] = clazz
    var entityType = PopulateHelper.getType(clazz)
    try {
      if (clazz.isInterface()) newClazz = entityType.clazz
      entity = newClazz.newInstance().asInstanceOf[Entity[_]]
    } catch {
      case e: Exception => throw new RuntimeException("[RequestUtil.extractConditions]: error in in initialize " + clazz)
    }
    var conditions = new collection.mutable.ListBuffer[Condition]()
    var params = Params.sub(prefix, exclusiveAttrNames)
    val paramIter = params.iterator
    while (paramIter.hasNext) {
      val entry = paramIter.next()
      val attr = entry._1
      val value = entry._2
      var strValue = value.toString.trim
      // 过滤空属性
      if (Strings.isNotEmpty(strValue)) {
        try {
          if (RESERVED_NULL && "null".equals(strValue)) {
            conditions += new Condition(prefix + "." + attr + " is null")
          } else {
            PopulateHelper.populator.populate(entity, entityType, attr, strValue)
            val v = Properties.get[Object](entity, attr) match {
              case Some(s) => s
              case None    => null
              case a: Any  => a
            }
            v match {
              case null       => logger.error("Error populate entity " + prefix + "'s attribute " + attr)
              case sv: String => conditions += new Condition(s"$prefix.$attr like :${attr.replace('.', '_')}", s"%$sv%")
              case sv         => conditions += new Condition(s"$prefix.$attr =:${attr.replace('.', '_')}", sv)
            }
          }
        } catch {
          case e: Exception => logger.error("Error populate entity " + prefix + "'s attribute " + attr, e)
        }
      }
    }
    conditions.toList
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
    var pageIndex = Params.getInt(PageParam) match {
      case Some(p) => p
      case None    => Params.getInt("pageIndex").getOrElse(Page.DefaultPageNo)
    }
    if (pageIndex < 1) Page.DefaultPageNo else pageIndex
  }

  /**
   * 获得请求中的页长
   */
  def pageSize: Int = {
    var pageSize = Params.get(PageSizeParam).getOrElse("")
    var pagesize = Page.DefaultPageSize
    if (Strings.isNotBlank(pageSize)) {
      pagesize = Numbers.toInt(pageSize.trim())
    } else {
      pageSize = CookieUtils.getCookieValue(ActionContext.current.request, PageSizeParam)
      if (Strings.isNotEmpty(pageSize)) pagesize = Numbers.toInt(pageSize)
    }
    if (pagesize < 1) Page.DefaultPageSize else pagesize
  }

  def addDateIntervalCondition(query: OqlBuilder[_], attr: String, beginOn: String, endOn: String) {
    addDateIntervalCondition(query, query.alias, attr, beginOn, endOn)
  }

  /**
   * 增加日期区间查询条件
   *
   * @param query
   * @param alias
   * @param attr 时间限制属性
   * @param beginOn 开始的属性名字(全名)
   * @param endOn 结束的属性名字(全名)
   * @throws ParseException
   */
  def addDateIntervalCondition(query: OqlBuilder[_], alias: String, attr: String, beginOn: String,
                               endOn: String) {
    val stime = Params.get(beginOn)
    val etime = Params.get(endOn)
    var df = new SimpleDateFormat("yyyy-MM-dd")
    val sdate: ju.Date = if (stime.isDefined) try { df.parse(stime.get) } catch { case e: ParseException => null } else null
    var edate: ju.Date = if (etime.isDefined) try { df.parse(etime.get) } catch { case e: ParseException => null } else null

    // 截至日期增加一天
    if (null != edate) {
      var gc = new ju.GregorianCalendar()
      gc.setTime(edate)
      gc.set(ju.Calendar.DAY_OF_YEAR, gc.get(ju.Calendar.DAY_OF_YEAR) + 1)
      edate = gc.getTime()
    }
    var objAttr = (if (null == alias) query.alias else alias) + "." + attr
    if (null != sdate && null == edate) {
      query.where(objAttr + " >=:sdate", sdate)
    } else if (null != sdate && null != edate) {
      query.where(objAttr + " >=:sdate and " + objAttr + " <:edate", sdate, edate)
    } else if (null == sdate && null != edate) {
      query.where(objAttr + " <:edate", edate)
    }
  }
}
