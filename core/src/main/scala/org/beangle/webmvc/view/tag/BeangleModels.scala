package org.beangle.webmvc.view.tag

import java.io.StringWriter
import java.{ util => ju }
import org.beangle.commons.collection.page.Page
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.view.component.{ Agent, Anchor, Checkbox, Checkboxes, ComponentContext, Css, Date, Dialog, Div, Email, Field, Foot, Form, Formfoot, Grid }
import org.beangle.webmvc.view.component.{ Head, Iframe, Messages, Module, Navitem, Navbar, Pagebar, Password, Radio, Radios, Reset, Select, Select2, Startend, Submit, Tab, Tabs, Textarea, Textfield, Textfields, Toolbar, Validity }
import org.beangle.webmvc.view.component.Grid.{ Bar, Boxcol, Col, Filter, Row, Treecol }
import freemarker.template.utility.StringUtil
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.view.UITheme
import org.beangle.webmvc.view.component.Navbar

class BeangleModels(context: ComponentContext, request: HttpServletRequest) extends AbstractModels(context, request) {

  val textResource = ContextHolder.context.textResource

  def url(url: String) = context.uriRender.render(request.getServletPath, url)

  def now = new ju.Date

  val uitheme = {
    val base = request.getContextPath
    val location = if (base.length() < 2) "/static/themes/"
    else base + "/static/themes/"
    new UITheme(location)
  }
  /**
   * query string and form control
   */
  def paramstring: String = {
    val sw = new StringWriter()
    val em = request.getParameterNames()
    while (em.hasMoreElements()) {
      val attr = em.nextElement()
      val value = request.getParameter(attr)
      if (!attr.equals("x-requested-with")) {
        sw.write(attr)
        sw.write('=')
        sw.write(StringUtil.javaScriptStringEnc(value))
        if (em.hasMoreElements()) sw.write('&')
      }
    }
    return sw.toString()
  }

  def isPage(data: Object) = data.isInstanceOf[Page[_]]

  def text(name: String) = textResource(name, name)

  def text(name: String, arg0: Object) {
    textResource(name, name, arg0)
  }

  def text(name: String, arg0: Object, arg1: Object) {
    textResource(name, name, arg0, arg1)
  }

  /**
   * Return useragent component.
   */
  def agent: TagModel = get(classOf[Agent])

  def head = get(classOf[Head])

  def dialog: TagModel = get(classOf[Dialog])

  def css: TagModel = get(classOf[Css])

  def iframe: TagModel = get(classOf[Iframe])

  def foot: TagModel = get(classOf[Foot])

  def form: TagModel = get(classOf[Form])

  def formfoot: TagModel = get(classOf[Formfoot])

  def submit: TagModel = get(classOf[Submit])

  def reset: TagModel = get(classOf[Reset])

  def toolbar: TagModel = get(classOf[Toolbar])

  def tabs: TagModel = get(classOf[Tabs])

  def tab: TagModel = get(classOf[Tab])

  def grid: TagModel = get(classOf[Grid])

  def gridbar: TagModel = get(classOf[Bar])

  def filter: TagModel = get(classOf[Filter])

  def row: TagModel = get(classOf[Row])

  def col: TagModel = {
    var model = models.get(classOf[Col])
    if (null == model) {
      // just for performance
      model = new TagModel(context) {
        override protected def getBean() = new Col(context)
      }
      models.put(classOf[Col], model)
    }
    return model
  }

  def treecol: TagModel = get(classOf[Treecol])

  def boxcol: TagModel = get(classOf[Boxcol])

  def pagebar: TagModel = get(classOf[Pagebar])

  def password: TagModel = get(classOf[Password])

  def a: TagModel = {
    var model = models.get(classOf[Anchor])
    if (null == model) {
      model = new TagModel(context) {
        override protected def getBean() = new Anchor(context)
      }
      models.put(classOf[Anchor], model)
    }
    return model
  }

  def messages: TagModel = get(classOf[Messages])

  def textfield: TagModel = get(classOf[Textfield])

  def email: TagModel = get(classOf[Email])

  def textarea: TagModel = get(classOf[Textarea])

  def field: TagModel = get(classOf[Field])

  def textfields: TagModel = get(classOf[Textfields])

  def datepicker: TagModel = get(classOf[Date])

  def div: TagModel = get(classOf[Div])

  def select: TagModel = get(classOf[Select])

  def select2: TagModel = get(classOf[Select2])

  def module: TagModel = get(classOf[Module])

  def navbar: TagModel = get(classOf[Navbar])

  def navitem: TagModel = get(classOf[Navitem])

  def radio: TagModel = get(classOf[Radio])

  def radios: TagModel = get(classOf[Radios])

  def startend: TagModel = get(classOf[Startend])

  def checkbox: TagModel = get(classOf[Checkbox])

  def checkboxes: TagModel = get(classOf[Checkboxes])

  def validity: TagModel = get(classOf[Validity])

}