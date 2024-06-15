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

package org.beangle.webmvc.view.tag

import org.beangle.template.api.ComponentContext

class Esign(context: ComponentContext) extends AbstractTextBean(context) {
  var lineWidth: String = "5"
  var height: String = "300"
  var width: String = "800"

  override def evaluateParams(): Unit = {
    super.evaluateParams()
    val f = findAncestor(classOf[Form])
    f.addCheck(s"sign.generate().then(function(res){ document.getElementById('${this.id}').value = res}).catch(function(err){alert(err)});")
  }
}
