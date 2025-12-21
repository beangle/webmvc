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
import org.beangle.transfer.importer.listener.ForeignerListener
import org.beangle.transfer.importer.{DefaultEntityImporter, ImportResult, ImportSetting}
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.support.helper.{ImportHelper, PopulateHelper}
import org.beangle.webmvc.view.View

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
    setting.domain = this.entityDao.domain
    setting.populator = PopulateHelper.populator
    setting.reader = ImportHelper.buildReader()
    val entityClazz = setting.domain.getEntity(this.entityClass).get.clazz
    setting.addEntityClazz(entityClazz, self.simpleEntityName)
    configImport(setting)
    val importer = DefaultEntityImporter(setting)
    put("importResult", tr)
    importer.transfer(tr)
    forward("/components/importData/result,result2")
  }

  protected def configImport(setting: ImportSetting): Unit = {
    setting.listeners = List(new ForeignerListener(entityDao))
  }

}
