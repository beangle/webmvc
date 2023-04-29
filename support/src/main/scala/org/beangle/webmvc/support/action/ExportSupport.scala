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

package org.beangle.webmvc.support.action

import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.data.dao.{LimitQuery, QueryPage}
import org.beangle.data.model.Entity
import org.beangle.data.transfer.Format
import org.beangle.data.transfer.excel.{ExcelItemWriter, ExcelTemplateExporter, ExcelTemplateWriter}
import org.beangle.data.transfer.exporter.{ExportContext, SimpleEntityExporter}
import org.beangle.web.action.annotation.{ignore, mapping}
import org.beangle.web.action.context.ActionContext
import org.beangle.web.action.context.Params.*
import org.beangle.web.action.view.{Status, View}
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.support.helper.PopulateHelper

trait ExportSupport[T <: Entity[_]] {
  self: EntityAction[T] =>
  /**
   * 导出
   */
  @mapping("export")
  def exportData(): View = {
    val response = ActionContext.current.response
    val ctx = new ExportContext
    get("template") match {
      case None =>
        ctx.exporter = new SimpleEntityExporter()
        ctx.writer = new ExcelItemWriter(ctx, response.getOutputStream)
        get("keys") foreach (ctx.put("keys", _))
        get("titles") foreach (ctx.put("titles", _))
        get("properties") foreach (ctx.put("properties", _))
        val format = get("format") match {
          case None => Format.Xlsx
          case Some(f) => Format.valueOf(Strings.capitalize(if (f == "xls") "xlsx" else f))
        }
        ctx.format = format
      case Some(template) =>
        ctx.format = Format.Xlsx
        ctx.exporter = new ExcelTemplateExporter()
        ctx.writer = new ExcelTemplateWriter(
          ClassLoaders.getResource(template).get, ctx, response.getOutputStream)
    }

    val ext = "." + Strings.uncapitalize(ctx.format.toString)
    val fileName =
      get("fileName") match {
        case Some(f) => if (!f.endsWith(ext)) f + ext else f
        case None => "exportFile" + ext
      }
    RequestUtils.setContentDisposition(response, fileName)
    configExport(ctx)
    ctx.exporter.exportData(ctx, ctx.writer)
    Status.Ok
  }

  @ignore
  protected def configExport(context: ExportContext): Unit = {
    val selectIds = getIds(simpleEntityName, PopulateHelper.getType(entityClass).id.clazz)
    val items =
      if (selectIds.isEmpty) {
        val builder = getQueryBuilder
        if (builder.hasGroupBy) {
          entityDao.search(builder.limit(null))
        } else {
          val query = builder.limit(1, 500)
          new QueryPage(query.build().asInstanceOf[LimitQuery[T]], entityDao)
        }
      } else {
        entityDao.findBy(entityClass, "id", selectIds)
      }
    context.put("items", items)
  }
}
