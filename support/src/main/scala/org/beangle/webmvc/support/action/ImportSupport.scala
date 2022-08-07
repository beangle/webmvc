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

import org.beangle.commons.lang.Strings
import org.beangle.data.model.Entity
import org.beangle.data.transfer.importer.listener.ForeignerListener
import org.beangle.data.transfer.importer.{DefaultEntityImporter, ImportResult, ImportSetting}
import org.beangle.web.action.view.View
import org.beangle.webmvc.support.helper.{ImportHelper, PopulateHelper}

trait ImportSupport[T <: Entity[_]] {
  self: EntityAction[T] =>

  def importForm(): View = {
    forward("/components/importData/form")
  }

  /**
   * 导入信息
   */
  def importData(): View = {
    val tr = new ImportResult()
    val setting = new ImportSetting
    val entityClazz = this.entityDao.domain.getEntity(this.entityClass).get.clazz
    val shortName = Strings.uncapitalize(Strings.substringAfterLast(entityClazz.getName, "."))
    setting.entityClazz = entityClazz
    setting.shortName = shortName
    setting.reader = ImportHelper.buildReader()
    configImport(setting)
    if (null == setting.importer) {
      val importer = new DefaultEntityImporter(setting.entityClazz, setting.shortName)
      importer.domain = this.entityDao.domain
      importer.populator = PopulateHelper.populator
      setting.importer = importer
      setting.listeners foreach { l =>
        importer.addListener(l)
      }
    }

    val importer = setting.importer
    if (null == setting.reader) {
      return forward("/components/importData/error")
    }
    try {
      importer.reader = setting.reader
      importer.transfer(tr)
      put("importer", importer)
      put("importResult", tr)
      if (tr.hasErrors) {
        forward("/components/importData/error")
      } else {
        forward("/components/importData/result")
      }
    } catch {
      case e: Exception =>
        logger.error("import error", e)
        tr.addFailure(getText("error.importformat"), e.getMessage)
        put("importResult", tr)
        forward("/components/importData/error")
    }
  }

  protected def configImport(setting: ImportSetting): Unit = {
    setting.listeners = List(new ForeignerListener(entityDao))
  }

}
