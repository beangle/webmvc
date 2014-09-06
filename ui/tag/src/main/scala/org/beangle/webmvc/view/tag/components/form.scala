package org.beangle.webmvc.view.tag.components

import java.io.Writer
import java.text.SimpleDateFormat
import java.{util => ju}

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.view.tag.ComponentContext

class Form(context: ComponentContext) extends ClosingUIBean(context) {
  var name: String = _
  var action: String = _
  var target: String = _

  var onsubmit: String = _

  /** Boolean */
  protected var validate: String = _

  var title: String = _

  private val elementChecks = new collection.mutable.HashMap[String, StringBuilder]

  private var extraChecks: StringBuilder = _

  override def evaluateParams(): Unit = {
    if (null == name && null == id) {
      generateIdIfEmpty()
      name = id
    } else if (null == id) {
      id = name
    }
    action = render(action)
    if (null != title) title = getText(title)
  }

  def getValidate(): String = {
    if (null == validate) {
      if (!elementChecks.isEmpty) validate = "true"
      else validate = "false"
    }
    return validate
  }

  def setValidate(validate: String): Unit = {
    this.validate = validate
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

  def addCheck(check: String): Unit = {
    if (null == extraChecks) extraChecks = new StringBuilder()
    extraChecks.append(check)
  }

  def getValidity(): String = {
    // every element initial validity buffer is 80 chars.
    val sb = new StringBuilder((elementChecks.size * 80) +
      (if (null == extraChecks) 0 else extraChecks.length))
    import scala.collection.JavaConversions._
    for ((key, value) <- elementChecks) {
      sb.append("jQuery('#").append(Strings.replace(key, ".", "\\\\.")).append("')")
        .append(value).append("\n")
    }
    if (null != extraChecks) sb.append(extraChecks)
    return sb.toString()
  }
}

class Formfoot(context: ComponentContext) extends ClosingUIBean(context) {

}

class Reset(context: ComponentContext) extends UIBean(context) {

}

class Submit(context: ComponentContext) extends UIBean(context) {
  var formId: String = _
  var onsubmit: String = _
  var action: String = _
  var value: String = _
  var target: String = _

  override def evaluateParams() {
    if (null == formId) {
      val f = findAncestor(classOf[Form])
      if (null != f) formId = f.id
    }
    if (null != onsubmit && -1 != onsubmit.indexOf('(')) onsubmit = Strings.concat("'", onsubmit, "'")
    if (null != value) value = getText(value)
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
  val DefaultItemMap = Map(("1", "是"), ("0", "否"))
  val DefaultKeys = List("1", "0")
  def booleanize(obj: Object): Object = Booleans.get(obj).getOrElse(obj)
}

class Radio(context: ComponentContext) extends UIBean(context) {

  var name: String = _
  var label: String = _
  var title: String = _
  var value: Object = ""

  override def evaluateParams() {
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)
    if (null != title) title = getText(title)
    else title = label
    this.value = Radio.booleanize(value)
  }
}

class Field(context: ComponentContext) extends ClosingUIBean(context) {
  var label: String = _
  var required: String = _
  override def evaluateParams() {
    if (null != label) label = getText(label)
  }
}
class AbstractTextBean(context: ComponentContext) extends UIBean(context) {
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

  override def evaluateParams() {
    super.evaluateParams()
    val myform = findAncestor(classOf[Form])
    if (null != maxlength) myform.addCheck(id, "maxLength(" + maxlength + ")")
  }
}
object Date {
  val ResvervedFormats = Map(("date",
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

  override def evaluateParams() {
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)

    if (null != title) title = getText(title)
    else title = label

    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      if ("true".equals(required)) myform.addRequire(id)
      if (null != check) myform.addCheck(id, check)
    }
    val format2 = Date.ResvervedFormats.getOrElse(format,format)
    if (null != format2) format = format2
    if (value.isInstanceOf[ju.Date]) {
      val dformat = new SimpleDateFormat(format)
      value = dformat.format(value.asInstanceOf[java.util.Date])
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

  override def evaluateParams() = {
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)

    if (null != title) title = getText(title)
    else title = label

    var myform = findAncestor(classOf[Form])
    if (null != myform) {
      if ("true".equals(required)) {
        myform.addCheck(id + "_span", "assert($(\"#" + id + ":checked\").length != 0,'必须勾选一项')")
      }
    }
  }
}

class Select(context: ComponentContext) extends ClosingUIBean(context) {
  var name: String = _
  var items: Object = _
  var empty: String = _
  var value: Object = _

  var keyName: String = _
  var valueName: String = _

  var label: String = _
  var title: String = _

  var comment: String = _
  var check: String = _
  var required: String = _

  /** option text template */
  var _option: String = _

  override def evaluateParams() {
    if (null == keyName) {
      if (items.isInstanceOf[ju.Map[_, _]]) {
        keyName = "key"
        valueName = "value"
        items = items.asInstanceOf[ju.Map[_, _]].entrySet
      } else {
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
      if ("true".equals(required)) myform.addRequire(id)
      if (null != check) myform.addCheck(id, check)
    }
    if (null == value) value = requestParameter(name)
    // trim empty string to null for speed up isSelected
    if ((value.isInstanceOf[String]) && Strings.isEmpty(value.asInstanceOf[String])) value = null
  }

  def isSelected(obj: Object): Boolean = {
    if (null == value) return false
    else try {
      var nobj = obj
      if (obj.isInstanceOf[Tuple2[_, _]]) nobj = obj.asInstanceOf[Tuple2[Object, _]]._1
      else if (obj.isInstanceOf[ju.Map.Entry[_, _]]) nobj = obj.asInstanceOf[ju.Map.Entry[Object, _]].getKey()
      val rs = value.equals(nobj) || value.equals(PropertyUtils.getProperty(nobj, keyName))
      return rs || value.toString().equals(nobj.toString()) ||
        value.toString().equals(String.valueOf(PropertyUtils.getProperty(nobj, keyName).toString))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        return false
    }
  }

  def option_=(o: String) {
    if (null != o) {
      if (Strings.contains(o, "$")) {
        this._option = o
      } else if (Strings.contains(o, ",")) {
        keyName = Strings.substringBefore(o, ",")
        valueName = Strings.substringAfter(o, ",")
      }
    }
  }
  def option = _option
}

class Email(context: ComponentContext) extends AbstractTextBean(context) {
  check = "match('email')"
}

class Password(context: ComponentContext) extends AbstractTextBean(context) {
  var minlength: String = "6"
  maxlength = "10"
  var showStrength = "false"
}