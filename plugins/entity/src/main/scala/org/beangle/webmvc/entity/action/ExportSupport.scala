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
package org.beangle.webmvc.entity.action
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.view.{ Status, View }
import org.beangle.data.transfer.Format
import org.beangle.data.transfer.excel.{ ExcelItemWriter, ExcelTemplateWriter, ExcelTemplateExporter }
import org.beangle.data.transfer.exporter.{ ExportContext, SimpleEntityExporter, ExportSetting }
import org.beangle.commons.lang.{ Strings, ClassLoaders }
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.{ ActionContext, Params }
import org.beangle.commons.web.util.RequestUtils

trait ExportSupport[T <: Entity[_]] {
  self: EntityAction[T] =>
  /**
   * 导出
   */
  def export(): View = {
    val response = ActionContext.current.response
    val setting = new ExportSetting
    val ctx = setting.context
    get("template") match {
      case None =>
        setting.exporter = new SimpleEntityExporter()
        setting.writer = new ExcelItemWriter(ctx, response.getOutputStream)
        get("keys") foreach (ctx.put("keys", _))
        get("titles") foreach (ctx.put("titles", _))
        get("properties") foreach (ctx.put("properties", _))
        val format = get("format") match {
          case None    => Format.Xls
          case Some(f) => Format.withName(Strings.capitalize(f))
        }
        ctx.format = format
      case Some(template) =>
        ctx.format = Format.Xls
        setting.exporter = new ExcelTemplateExporter()
        setting.writer = new ExcelTemplateWriter(
          ClassLoaders.getResource(template).get, ctx, response.getOutputStream)
    }

    val ext = "." + Strings.uncapitalize(ctx.format.toString)
    val fileName =
      get("fileName") match {
        case Some(f) => if (!f.endsWith(ext)) f + ext else f
        case None    => "exportFile" + ext
      }
    RequestUtils.setContentDisposition(response, fileName)
    configExport(setting)
    setting.exporter.export(ctx, setting.writer)
    Status.Ok
  }

  @ignore
  def configExport(setting: ExportSetting) {
    setting.context.put("items", entityDao.search(getQueryBuilder().limit(null)))
  }
}
