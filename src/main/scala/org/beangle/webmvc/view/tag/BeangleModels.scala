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

import jakarta.servlet.http.HttpServletRequest
import org.beangle.template.api.{ComponentContext, Tag}
import org.beangle.web.action.dispatch.ActionUriRender
import org.beangle.web.action.view.Static

class BeangleModels(context: ComponentContext, request: HttpServletRequest) extends CoreModels(context, request) {

  val static_base: String = {
    Static.Default.base
  }

  def static: Static = {
    Static.Default
  }

  def static_url(bundle: String, filename: String): String = {
    Static.Default.url(bundle, filename)
  }

  def script(bundle: String, fileName: String): String = {
    script(bundle, fileName, false)
  }

  def script(bundle: String, fileName: String, deferable: Boolean): String = {
    Static.Default.script(bundle, fileName, deferable)
  }

  def css(bundle: String, fileName: String): String = {
    Static.Default.css(bundle, fileName)
  }

  val rest = new Rest(this.context.services("uriRender").asInstanceOf[ActionUriRender])

  def math: MathOps.type = MathOps

  def head: Tag = get(classOf[Head])

  def dialog: Tag = get(classOf[Dialog])

  def iframe: Tag = get(classOf[Iframe])

  def foot: Tag = get(classOf[Foot])

  def form: Tag = get(classOf[Form])

  def fieldset: Tag = get(classOf[Fieldset])

  def formfoot: Tag = get(classOf[Formfoot])

  def submit: Tag = get(classOf[Submit])

  def reset: Tag = get(classOf[Reset])

  def toolbar: Tag = get(classOf[Toolbar])

  def tabs: Tag = get(classOf[Tabs])

  def tab: Tag = get(classOf[Tab])

  def grid: Tag = get(classOf[Grid])

  def gridbar: Tag = get(classOf[Grid.Bar])

  def filter: Tag = get(classOf[Grid.Filter])

  def row: Tag = get(classOf[Grid.Row])

  def col: Tag = get(classOf[Grid.Col])

  def treecol: Tag = get(classOf[Grid.Treecol])

  def boxcol: Tag = get(classOf[Grid.Boxcol])

  def pagebar: Tag = get(classOf[Pagebar])

  def password: Tag = get(classOf[Password])

  def a: Tag = get(classOf[Anchor])

  def messages: Tag = get(classOf[Messages])

  def textfield: Tag = get(classOf[Textfield])

  def email: Tag = get(classOf[Email])

  def cellphone: Tag = get(classOf[Cellphone])

  def number: Tag = get(classOf[Number])

  def range: Tag = get(classOf[Range])

  def time: Tag = get(classOf[Time])

  def textarea: Tag = get(classOf[Textarea])

  def editor: Tag = get(classOf[Editor])

  def field: Tag = get(classOf[Field])

  def textfields: Tag = get(classOf[Textfields])

  def hairline: Tag = get(classOf[HairLine])

  def date: Tag = get(classOf[Date])

  def div: Tag = get(classOf[Div])

  def select: Tag = get(classOf[Select])

  def select2: Tag = get(classOf[Select2])

  def combobox: Tag = get(classOf[Combobox])

  def navbar: Tag = get(classOf[Navbar])

  def nav: Tag = get(classOf[Nav])

  def navitem: Tag = get(classOf[Navitem])

  def radio: Tag = get(classOf[Radio])

  def radios: Tag = get(classOf[Radios])

  def startend: Tag = get(classOf[Startend])

  def checkbox: Tag = get(classOf[Checkbox])

  def checkboxes: Tag = get(classOf[Checkboxes])

  def validity: Tag = get(classOf[Validity])

  def file: Tag = get(classOf[File])

  def URL: Tag = get(classOf[Url])

  def card: Tag = get(classOf[Card])

  def card_header: Tag = get(classOf[CardHeader])

  def card_tools: Tag = get(classOf[CardTools])

  def card_body: Tag = get(classOf[CardBody])

  def card_footer: Tag = get(classOf[CardFooter])

  def esign: Tag = get(classOf[Esign])
}
