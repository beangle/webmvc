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

import org.beangle.data.model.Entity
import org.beangle.doc.transfer.importer.listener.ForeignerListener
import org.beangle.doc.transfer.importer.{DefaultEntityImporter, ImportResult, ImportSetting}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View
import org.beangle.webmvc.support.helper.{ImportHelper, PopulateHelper}

trait ImportSupport[T <: Entity[_]] {
  self: ActionSupport with EntityAction[T] =>

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
    val shortName = self.simpleEntityName
    setting.entityClazz = entityClazz
    setting.shortName = shortName
    setting.reader = ImportHelper.buildReader()
    configImport(setting)
    if (null == setting.importer) {
      val importer = new DefaultEntityImporter(setting.entityClazz, setting.shortName)
      importer.stopOnError = setting.stopOnError
      importer.domain = this.entityDao.domain
      importer.populator = PopulateHelper.populator
      setting.importer = importer
      setting.listeners foreach importer.addListener
    }

    val importer = setting.importer
    importer.reader = setting.reader
    put("importer", importer)
    put("importResult", tr)
    if null == importer.reader then tr.addFailure("reading error", "Cannot build importer reader")
    else importer.transfer(tr)
    forward("/components/importData/result,result2")
  }

  protected def configImport(setting: ImportSetting): Unit = {
    setting.listeners = List(new ForeignerListener(entityDao))
  }

}
