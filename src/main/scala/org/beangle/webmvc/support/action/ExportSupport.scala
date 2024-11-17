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

import org.beangle.commons.lang.SystemInfo.properties
import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.text.i18n.Messages
import org.beangle.data.dao.{LimitQuery, QueryPage}
import org.beangle.data.model.Entity
import org.beangle.doc.transfer.Format
import org.beangle.doc.transfer.exporter.ExportContext
import org.beangle.webmvc.annotation.{ignore, mapping, response}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.context.Params.*
import org.beangle.webmvc.view.{Status, View}
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.support.helper.PopulateHelper

trait ExportSupport[T <: Entity[_]] {
  self: EntityAction[T] =>
  /**
   * 导出
   */
  @mapping("export")
  def exportData(): View = {
    val ctx = get("template") match {
      case None =>
        val titles = get("titles").orElse(get("properties")).getOrElse("")
        val messages = Messages(ActionContext.current.locale)
        val properties = Strings.split(titles).toSeq.map(p => if p.contains(":") then p else p + ":" + messages.get(this.entityClass, p))
        val format = Format.valueOf(Strings.capitalize(get("format").getOrElse("xlsx")))
        val ctx = if format == Format.Csv then ExportContext.csv(properties) else ExportContext.excel(None, properties)
        ctx.header(None, properties).exportAsString(getBoolean("convertToString", false))
      case Some(template) => ExportContext.template(ClassLoaders.getResource(template).get)
    }
    configExport(ctx)
    val response = ActionContext.current.response
    RequestUtils.setContentDisposition(response, ctx.buildFileName(get("fileName")))
    ctx.writeTo(response.getOutputStream)
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
    context.setItems(items)
  }
}
