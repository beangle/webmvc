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
import org.beangle.commons.lang.{Numbers, Strings}
import org.beangle.template.api.{ComponentContext,UIBean}

import scala.jdk.javaapi.CollectionConverters.asScala

class Textfields(context: ComponentContext) extends UIBean(context) {
  var names: String = _
  var fields: Array[Textfield] = _

  override def evaluateParams(): Unit = {
    val nameArray = Strings.split(names, ',')
    fields = new Array[Textfield](nameArray.length)
    nameArray.indices foreach { i =>
      fields(i) = new Textfield(context)
      var name = nameArray(i)
      var title = name
      val semiconIndex = name.indexOf(';')
      if (-1 != semiconIndex) {
        title = name.substring(semiconIndex + 1)
        name = name.substring(0, semiconIndex)
      }
      fields(i).name = name
      fields(i).label = title
      fields(i).evaluateParams()
    }
  }
}

class Radios(context: ComponentContext) extends UIBean(context) {
  var name: String = _

  var label: String = _

  var items: Object = _

  var radios: Array[Radio] = _

  var value: Object = _

  var comment: String = _

  override def evaluateParams(): Unit = {
    if (null == this.id) generateIdIfEmpty()
    label = processLabel(label, name)

    val keys = convertItems()
    radios = new Array[Radio](keys.size)
    var i = 0
    for (key <- keys) {
      radios(i) = new Radio(context)
      radios(i).title = String.valueOf(items.asInstanceOf[collection.Map[Any, _]](key))
      radios(i).value = key.asInstanceOf[Object]
      radios(i).id = Strings.concat(this.id + "_" + String.valueOf(i))
      radios(i).evaluateParams()
      i += 1
    }
    if (null == this.value && radios.length > 0) this.value = radios(0).value
    this.value = Radio.booleanize(this.value)

  }

  private def convertItems(): Iterable[_] = {
    import Radio._
    items match {
      case m: collection.Map[_, _] =>
        m.keys.toList
      case jm: java.util.Map[_, _] =>
        items = asScala(jm)
        items.asInstanceOf[collection.Map[_, _]].keys.toList
      case null =>
        items = DefaultItemMap
        DefaultKeys
      case s: String =>
        if (Strings.isBlank(s)) {
          items = DefaultItemMap
          DefaultKeys
        } else {
          val newkeys = new collection.mutable.ListBuffer[Object]
          val itemMap = new collection.mutable.HashMap[Object, Object]
          val titleArray = Strings.split(items.toString, ',')
          titleArray.indices foreach { i =>
            val titleValue = titleArray(i)
            val semiconIndex = titleValue.indexOf(':')
            if (-1 != semiconIndex) {
              newkeys += titleValue.substring(0, semiconIndex)
              itemMap.put(titleValue.substring(0, semiconIndex), titleValue.substring(semiconIndex + 1))
            }
          }
          items = itemMap
          newkeys
        }
    }
  }
}

class Checkboxes(context: ComponentContext) extends UIBean(context) {
  var name: String = _
  var label: String = _
  var items: Object = _
  var checkboxes: Array[Checkbox] = _
  var value: Object = _
  var comment: String = _
  var required: String = _
  var min: Object = _
  var max: Object = _
  var valueName = "name"

  override def evaluateParams(): Unit = {
    if (null == this.id) generateIdIfEmpty()
    label = getText(label)

    val keys = convertItems()
    val values = convertValue()
    checkboxes = new Array[Checkbox](keys.size)
    var i = 0
    val myform = findAncestor(classOf[Form])
    if (required == "true" && min == null) {
      min = "1"
    }
    if (null == max) {
      max = checkboxes.length.toString
    }
    val minValue = getValidateNum(min)
    val maxValue = getValidateNum(max)
    if (null != myform) {
      if ("true".equals(required)) {
        myform.addCheck(id, "assert($(\"input[name='" + name + "']:checked\").length != 0,'必须勾选一项')")
      }
    }
    assert(minValue <= maxValue)
    if (minValue > 0 && minValue <= checkboxes.length) {
      myform.addCheck(id, "assert($(\"input[name='" + name + "']:checked\").length >= " + minValue + ",'至少勾选"
        + minValue + "项')")
    }
    if (maxValue < checkboxes.length && maxValue > 0) {
      myform.addCheck(id, "assert($(\"input[name='" + name + "']:checked\").length <= " + maxValue + ",'最多勾选"
        + maxValue + "项')")
    }

    val itemsMap = items.asInstanceOf[collection.Map[Any, Any]]
    for (key <- keys) {
      checkboxes(i) = new Checkbox(context)
      checkboxes(i).title = String.valueOf(itemsMap(key))
      checkboxes(i).value = key
      checkboxes(i).id = Strings.concat(this.id + "_" + String.valueOf(i))
      checkboxes(i).checked = values.contains(key)
      checkboxes(i).evaluateParams()
      i = i + 1
    }
  }

  private def convertItems(): Iterable[Object] = {
    if (items.isInstanceOf[collection.Map[_, _]]) return items.asInstanceOf[collection.Map[Object, Object]].keys
    val itemMap = new collection.mutable.HashMap[Object, Object]
    val keys = new collection.mutable.ListBuffer[Object]
    items match {
      case s: String =>
        if (Strings.isBlank(s)) {
          List.empty
        } else {
          val titleArray = Strings.split(items.toString, ',')
          titleArray.indices foreach { i =>
            val titleValue = titleArray(i)
            val semiconIndex = titleValue.indexOf(':')
            if (-1 != semiconIndex) {
              keys += titleValue.substring(0, semiconIndex)
              itemMap.put(titleValue.substring(0, semiconIndex), titleValue.substring(semiconIndex + 1))
            }
          }
          items = itemMap
          keys
        }
      case i: Iterable[_] =>
        i foreach { obj =>
          val value = Properties.get[Object](obj, "id")
          val title = Properties.get[Object](obj, valueName)
          keys += value
          itemMap.put(value, title)
        }
        items = itemMap
        keys
    }
  }

  private def getValidateNum(number: Object): Int = {
    Numbers.toInt(number.toString)
  }

  private def convertValue(): collection.Set[Object] = {
    value match {
      case null => Set.empty
      case iter: java.lang.Iterable[_] =>
        (for (obj <- asScala(iter)) yield Properties.get[Object](obj, "id")).toSet
      case iter: Iterable[_] =>
        (for (obj <- iter) yield Properties.get[Object](obj, "id")).toSet
      case arry: Array[Object] => arry.toSet
      case str: String => if (Strings.isNotBlank(str)) Strings.split(str).toSet else Set.empty
    }
  }
}

class Select2(context: ComponentContext) extends UIBean(context) {
  var keyName = "id"
  var valueName = "name"
  var label: String = _
  var required: String = _
  var name1st: String = _
  var name2nd: String = _
  var items1st: Object = _
  var items2nd: Object = _
  var size = "10"

  var style = "width:250px;height:200px"

  override def evaluateParams(): Unit = {
    label = getText(label)
    generateIdIfEmpty()
    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      val mySelectId = id + "_1"
      if ("true".equals(required)) {
        myform.addCheck(mySelectId, "assert(bg.select.selectAll,'requireSelect')")
      } else {
        myform.addCheck(mySelectId,
          Strings.concat("assert(bg.select.selectAll(document.getElementById('", mySelectId, "'))||true)"))
      }
    }

  }

  def setOption(option: String): Unit = {
    if (null != option) {
      if (Strings.contains(option, ",")) {
        keyName = Strings.substringBefore(option, ",")
        valueName = Strings.substringAfter(option, ",")
      }
    }
  }
}

class Startend(context: ComponentContext) extends UIBean(context) {
  var label: String = _

  var name: String = _

  var start: Object = _

  var end: Object = _

  var comment: String = _

  var required: String = _

  var format = "yyyy-MM-dd"

  var dates: Array[Date] = _

  override def evaluateParams(): Unit = {
    val nameArray = Strings.split(name, ',')
    dates = new Array[Date](nameArray.length)
    Date.ResvervedFormats.get(format) foreach { f => format = f }
    val requiredArray = Strings.split(required, ',')
    val commentArray = Strings.split(comment, ',')
    val labelArray = Strings.split(label, ',')
    nameArray.indices foreach { i =>
      if (i < 2) {
        dates(i) = new Date(context)
        val name = nameArray(i)
        dates(i).name = name
        dates(i).format = format
        if (requiredArray != null) {
          dates(i).required = if (requiredArray.length == 1) required else requiredArray(i)
        }
        if (commentArray != null) {
          dates(i).comment = if (commentArray.length == 1) comment else commentArray(i)
        }
        if (labelArray != null) {
          dates(i).label = if (labelArray.length == 1) label else labelArray(i)
        }
        dates(i).title = dates(i).label
        if (i == 0) dates(0).value = start
        else dates(1).value = end

        dates(i).evaluateParams()
      }
    }
    if (dates.length == 2) {
      dates(0).maxDate = "#F{$dp.$D(\\'" + dates(1).id + "\\')}"
      dates(1).minDate = "#F{$dp.$D(\\'" + dates(0).id + "\\')}"

      if (labelArray.length == 1) {
        val containTime = format.contains("HH:mm")
        dates(0).title = dates(0).title + getText(if (containTime) "common.beginAt" else "common.beginOn")
        dates(1).title = dates(1).title + getText(if (containTime) "common.endAt" else "common.endOn")
      }
    }
  }
}
