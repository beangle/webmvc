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

import org.beangle.commons.bean.Properties
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{Numbers, Primitives, Strings}
import org.beangle.template.api.{ClosingUIBean, ComponentContext, UIBean}

import java.io.Writer
import java.text.SimpleDateFormat
import java.util as ju

class Form(context: ComponentContext) extends ActionClosingUIBean(context) {
  var name: String = _
  var action: String = _
  var target: String = _
  var method: String = "post"
  var enctype: String = _
  var onsubmit: String = _

  /** Boolean */
  private var _validate: String = _

  var title: String = _

  private val elementChecks = new collection.mutable.HashMap[String, StringBuilder]

  private var extraChecks: StringBuilder = _

  var scripts: String = _

  override def evaluateParams(): Unit = {
    if (null == name && null == id) {
      generateIdIfEmpty()
      name = id
    } else if (null == id) {
      id = name
    }
    if (null != action) action = render(action)
    title = getText(title)
    if (null != onsubmit) {
      onsubmit = onsubmit.trim()
      if (!onsubmit.contains(')') && !onsubmit.contains(' ')) {
        onsubmit = onsubmit + "(document." + id + ")"
      }
      if (!onsubmit.startsWith("return")) {
        onsubmit = "return " + onsubmit
      }
    }
  }

  def validate: String = {
    if (null == _validate) {
      if (elementChecks.nonEmpty || (null != extraChecks && extraChecks.nonEmpty)) _validate = "true"
      else _validate = "false"
    }
    _validate
  }

  def validate_=(validate: String): Unit = {
    this._validate = validate
  }

  /**
    * Required element by id
    */
  def addRequire(id: String): Unit = this.addCheck(id, "require().match('notBlank')")

  def addCheck(id: String, check: String): Unit = {
    elementChecks.get(id) match {
      case Some(sb) => sb.append('.').append(check)
      case None =>
        val sb = new StringBuilder(100)
        elementChecks.put(id, sb)
        sb.append('.').append(check)
    }
  }

  def addScript(script: String): Unit = {
    if (null == scripts) scripts = script
    else scripts = (scripts + "\n" + script)
  }

  def addCheck(check: String): Unit = {
    if (null == extraChecks) extraChecks = new StringBuilder()
    extraChecks.append(check)
  }

  def validity: String = {
    // every element initial validity buffer is 80 chars.
    val sb = new StringBuilder((elementChecks.size * 80) +
      (if (null == extraChecks) 0 else extraChecks.length))
    for ((key, value) <- elementChecks) {
      sb.append("jQuery('#").append(Strings.replace(key, ".", "\\\\.")).append("')")
        .append(value).append("\n")
    }
    if (null != extraChecks) sb.append(extraChecks)
    sb.toString
  }
}

class Formfoot(context: ComponentContext) extends ClosingUIBean(context) {

}

class Reset(context: ComponentContext) extends UIBean(context) {

}

class Submit(context: ComponentContext) extends ActionUIBean(context) {
  var formId: String = _
  var onsubmit: String = _
  var action: String = _
  var value: String = _
  var target: String = _

  override def evaluateParams(): Unit = {
    if (null == formId) {
      val f = findAncestor(classOf[Form])
      if (null != f) formId = f.id
    }
    if (null != onsubmit && -1 != onsubmit.indexOf('(')) onsubmit = Strings.concat("'", onsubmit, "'")
    if (null == value) value = "action.submit"
    value = getText(value)
    if (null != action) action = render(action)
  }
}

class Validity(context: ComponentContext) extends ClosingUIBean(context) {

  override def doEnd(writer: Writer, body: String): Boolean = {
    val myform = findAncestor(classOf[Form])
    if (null != myform) myform.addCheck(body)
    false
  }
}

object Radio {
  val Booleans: Map[Any, String] = Map(true -> "1", false -> "0", "y" -> "1", "Y" -> "1", "true" -> "1", "false" -> "0", "n" -> "0", "N" -> "0")
  val DefaultItemMap: Map[String, String] = Map(("1", "是"), ("0", "否"))
  val DefaultKeys: List[String] = List("1", "0")

  def booleanize(obj: Object): Object = Booleans.getOrElse(obj, obj)
}

class Radio(context: ComponentContext) extends UIBean(context) {

  var name: String = _
  var label: String = _
  var title: String = _
  var value: Object = ""

  override def evaluateParams(): Unit = {
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)
    if (null != title) title = getText(title)
    else title = label
    this.value = Radio.booleanize(value)
  }
}

class Fieldset(context: ComponentContext) extends ClosingUIBean(context) {
  var title: String = _
}

class Field(context: ComponentContext) extends ClosingUIBean(context) {
  var label: String = _
  var required: String = _

  override def evaluateParams(): Unit = {
    label = getText(label)
  }
}

class AbstractTextBean(context: ComponentContext) extends ClosingUIBean(context) {
  var name: String = _
  var label: String = _
  var title: String = _
  var comment: String = _
  var required: String = _
  var value: Object = ""
  var check: String = _
  var maxlength = "100"

  override def evaluateParams(): Unit = {
    generateIdIfEmpty()
    label = processLabel(label, name)
    if (null != title) {
      title = getText(title)
    } else {
      title = label
    }
    val myform = findAncestor(classOf[Form])
    if ("true".equals(required)) myform.addRequire(id)
    if (null != check) myform.addCheck(id, check)
  }
}

class Textfield(context: ComponentContext) extends AbstractTextBean(context)

class Textarea(context: ComponentContext) extends AbstractTextBean(context) {
  var cols: String = _
  var readonly: String = _
  var rows: String = _
  var wrap: String = _

  maxlength = "400"

  override def evaluateParams(): Unit = {
    super.evaluateParams()
    val myform = findAncestor(classOf[Form])
    if (null != maxlength) myform.addCheck(id, "maxLength(" + maxlength + ")")
  }
}

object Date {
  val ResvervedFormats: Map[String, String] = Map(("date",
    "yyyy-MM-dd"), ("datetime", "yyyy-MM-dd HH:mm:ss"))
}

class Date(context: ComponentContext) extends UIBean(context) {
  var name: String = _
  var label: String = _
  var title: String = _
  var comment: String = _
  var check: String = _
  var required: String = _
  var value: Object = ""
  var format = "date"
  var minDate: String = _
  var maxDate: String = _

  override def evaluateParams(): Unit = {
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)

    if (null != title) title = getText(title)
    else title = label

    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      if ("true".equals(required)) myform.addRequire(id)
      if (null != check) myform.addCheck(id, check)
    }
    val format2 = Date.ResvervedFormats.getOrElse(format, format)
    if (null != format2) format = format2
    value match {
      case juDate: ju.Date =>
        val dformat = new SimpleDateFormat(format)
        value = dformat.format(juDate)
      case _ =>
    }
  }

}

class Checkbox(context: ComponentContext) extends UIBean(context) {
  var name: String = _
  var label: String = _
  var title: String = _
  var value: Object = ""
  var checked = false
  var required: String = _

  override def evaluateParams(): Unit = {
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)

    if (null != title) title = getText(title)
    else title = label

    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      if ("true".equals(required)) {
        myform.addCheck(id + "_span", "assert($(\"#" + id + ":checked\").length != 0,'必须勾选一项')")
      }
    }
  }
}

class Select(context: ComponentContext) extends ActionClosingUIBean(context) {
  var name: String = _
  var items: Object = _
  var empty: String = _

  val keys = Collections.newSet[String]
  var values: Object = _

  var keyName: String = _
  var valueName: String = _

  var label: String = _
  var title: String = _

  var comment: String = _
  var check: String = _
  var required: String = _

  /** option text template */
  var _option: String = _

  var href: String = _

  var multiple: String = _

  var chosenMin: String = "30"

  var width: String = _

  override def evaluateParams(): Unit = {
    if (null == keyName) {
      items match {
        case juMap: ju.Map[_, _] =>
          keyName = "key"
          valueName = "value"
          items = juMap.entrySet
        case _ =>
          keyName = "id"
          valueName = "name"
      }
    }
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)
    if (null != title) title = getText(title)
    else title = label

    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      if ("true" == required) myform.addRequire(id)
      if (null != check) myform.addCheck(id, check)
    }
    if (null == empty && "true" != required && multiple != "true") empty = "..."
    if ("true" == multiple) empty = null
    if (null == values) setValue(requestParameter(name))
    if (null != values) collectKeys()

    if (null == width) {
      this.parameters.get("style") foreach { style =>
        //style="width:xx;"
        val w = Strings.substringAfter(style.toString, "width:")
        width =
          if (w.contains(";")) {
            Strings.substringBefore(w, ";")
          } else {
            w
          }
      }
    }
    if ("true" == multiple && width == null) width = "400px" //防止ajaxchosen引起宽度异常问题
    if (null != href) href = render(href)
  }

  private def collectKeys(): Unit = {
    val emptyValues = Collections.newBuffer[Object]
    val valueList = values match {
      case ji: java.lang.Iterable[_] => scala.jdk.javaapi.CollectionConverters.asScala(ji)
      case si: Iterable[_] => si
    }
    valueList.foreach { value =>
      val k = value match {
        case str: String => if (Strings.isEmpty(str)) null else str
        case tuple: (_, _) => tuple._1
        case _ =>
          if (Primitives.isWrapperType(value.getClass)) value
          else {
            if keyName == "key" then value else Properties.get[Any](value, keyName)
          }
      }
      k match {
        case null => emptyValues += value
        case s: String => keys.add(s)
        case _ => keys.add(k.toString)
      }
    }
    if emptyValues.nonEmpty then values = valueList.toBuffer.subtractAll(emptyValues)
    if multiple != "true" && keys.size > 1 then multiple = "true"
  }

  def setValue(v: Object): Unit = {
    v match {
      case null => this.values = List.empty
      case s: String => if (Strings.isEmpty(s)) {
        this.values = List.empty
      } else {
        this.values = List(s.trim)
      }
      case _ => this.values = List(v)
    }
  }

  def isSelected(obj: Object): Boolean = {
    if (null == values) {
      false
    } else {
      try {
        val itemKey = obj match {
          case tu: (_, _) => tu._1.toString
          case e: ju.Map.Entry[_, _] => e.getKey.toString
          case _ => Properties.get[Any](obj, keyName).toString
        }
        keys.contains(itemKey)
      } catch {
        case _: Exception => false
      }
    }
  }

  def option_=(o: String): Unit = {
    if (null != o) {
      if (Strings.contains(o, "$")) {
        this._option = o
      } else if (Strings.contains(o, ",")) {
        keyName = Strings.substringBefore(o, ",")
        valueName = Strings.substringAfter(o, ",")
      }
    }
  }

  def option: String = _option

  def remoteSearch: Boolean = {
    Strings.contains(href, "={term}") || Strings.contains(href, "=%7Bterm%7D")
  }

}

class Email(context: ComponentContext) extends AbstractTextBean(context) {
  check = "match('email')"
}

class Cellphone(context: ComponentContext) extends AbstractTextBean(context) {

  override def evaluateParams(): Unit = {
    super.evaluateParams()

    if (!parameters.contains("onchange")) {
      addParameter("onchange", "checkMobile(this)")
    }
    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      myform.addScript(
        """
          |function validateMobile(v){
          |  return /^1(3[0-9]|4[01456879]|5[0-3,5-9]|6[2567]|7[0-8]|8[0-9]|9[0-3,5-9])\d{8}$/.test(v)
          |}
          |function checkMobile(elem){
          |  var row = jQuery(elem).parent();
          |  if(elem.value){
          |    if(!validateMobile(elem.value)){
          |      raiseValidateError(row,"请正确填写手机号");
          |    }else{
          |      row.find("label.error").remove();
          |    }
          |  }else{
          |    raiseValidateError(row,"请填写手机号");
          |  }
          |}
          |function raiseValidateError(row,msg){
          |  row.find("label.error").remove();
          |  row.append('<label class="error">'+msg+'</label>');
          |  return false;
          |}
          |""".stripMargin
      )
      myform.addCheck(id, s"match(validateMobile,'请正确填写手机号')")
    }
  }
}

class Number(context: ComponentContext) extends AbstractTextBean(context) {
  check = "match('integer')"
  var min: String = "0"
  var max: String = "1000"

  override def evaluateParams(): Unit = {
    check = s"match('integer').range($min,$max)"
    super.evaluateParams()
  }
}

class Range(context: ComponentContext) extends AbstractTextBean(context) {
  var min: String = "0"
  var max: String = "1000"

  override def evaluateParams(): Unit = {
    check = s"match('integer').range($min,$max)"
    super.evaluateParams()
  }
}

class Time(context: ComponentContext) extends AbstractTextBean(context) {
}

class Password(context: ComponentContext) extends AbstractTextBean(context) {
  var minlength: String = "6"
  maxlength = "10"
  var showStrength = "false"
}

class File(context: ComponentContext) extends AbstractTextBean(context) {
  var extensions: String = ""
  var maxSize = "5M" //1M

  override def evaluateParams(): Unit = {
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)
    if (null != title) title = getText(title)
    else title = label

    var maxSizeStr = maxSize.toLowerCase().trim()
    if (maxSizeStr.endsWith("b")) {
      maxSizeStr = Strings.substringBefore(maxSizeStr, "b")
    }
    if (maxSizeStr.endsWith("k")) {
      maxSize = Strings.substringBefore(maxSizeStr, "k")
    } else if (maxSizeStr.endsWith("m")) {
      maxSize = (Numbers.toLong(Strings.substringBefore(maxSizeStr, "m")) * 1024).toString;
    }
    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      if (Strings.isEmpty(myform.enctype)) {
        myform.enctype = "multipart/form-data"
      }
      if ("true".equals(required)) myform.addRequire(id)

      val extRegex =
        if (Strings.isNotBlank(extensions)) {
          val a = Strings.split(extensions, ",") map { ext => s"""(\\.${ext})""" }
          a.mkString("|")
        } else {
          ".*"
        }
      myform.addCheck(
        s"""
           |function checkFile_${id}(value){
           |  if(jQuery("#${id}").data("file").size > ${maxSize}*1024){
           |    return false;
           |  }
           |  return /${extRegex}/.test(value);
           |}
           |""".stripMargin
      )
      myform.addCheck(id, s"match(checkFile_${id},'文件格式或大小不符合要求')")
    }
  }

}

class Url(context: ComponentContext) extends AbstractTextBean(context) {
  check = "match('url')"
}

class Combobox(context: ComponentContext) extends Select(context) {
  override def evaluateParams(): Unit = {
    super.evaluateParams()
    this.multiple = null
    this.chosenMin = "1"
    if (null == this.empty) this.empty = "..."
    require(null != href)
  }

}
