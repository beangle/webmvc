/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.webmvc.context.impl

import org.beangle.commons.cdi.Container
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.context.ActionFinder

/**
 * Find actions from application context
 */
@description("基于Container的Action自动发现者")
class ContainerActionFinder(val container: Container) extends ActionFinder {

  override def actions(actionTest: ActionFinder.Test): Seq[Object] = {
    val actions = new collection.mutable.ListBuffer[Object]
    container.keys() foreach { name =>
      val bean: Object = container.getBean(name).get
      if (actionTest.apply(bean.getClass)) actions += bean
    }
    actions
  }
}