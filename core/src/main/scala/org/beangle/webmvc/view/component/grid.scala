package org.beangle.webmvc.view.component

import java.io.Writer
import java.{ util => ju }

import org.beangle.commons.collection.page.Page
import org.beangle.commons.lang.{ Objects, Strings }
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.view.template.Themes

import freemarker.template.utility.StringUtil
object Grid {

  class Filter(context: ComponentContext) extends ClosingUIBean(context) {
    var property: String = _

    override def doEnd(writer: Writer, body: String): Boolean = {
      val grid = findAncestor(classOf[Grid])
      if (null != property && null != grid) {
        grid.filters.put(property, body)
      }
      false
    }
  }

  class Bar(context: ComponentContext) extends ClosingUIBean(context) {
    val grid: Grid = findAncestor(classOf[Grid])

    override def doEnd(writer: Writer, body: String): Boolean = {
      grid.bar = body
      false
    }
  }

  class Row(context: ComponentContext) extends IterableUIBean(context) {
    val table = findAncestor(classOf[Grid])
    val var_index = table.`var` + "_index"
    var index = -1
    var curObj: Any = _
    var innerTr: Option[Boolean] = None

    private val iterator: Iterator[Any] = {
      table.items match {
        case iterbl: Iterable[_] => if (iterbl.iterator.hasNext) iterbl.iterator else List(null).iterator
        case javaIter: ju.Collection[_] => collection.JavaConversions.asScalaIterator(javaIter.iterator())
      }
    }

    def isHasTr(): Boolean = {
      innerTr.getOrElse({
        var i = 0
        var innerTr = false
        var doWhile = true
        // max try count is 10
        while (doWhile && i < body.length() && i < 10) {
          if (body.charAt(i) == '<' && Strings.substring(body, i, i + 3).equals("<tr")) {
            innerTr = true
            doWhile = false
          }
          i = i + 1
        }
        this.innerTr = Some(innerTr)
        innerTr
      })
    }

    override protected def next(): Boolean = {
      val ctx = ContextHolder.context
      if (iterator != null && iterator.hasNext) {
        index = index + 1
        curObj = iterator.next()
        ctx.attribute(table.`var`, curObj)
        ctx.attribute(var_index, index.asInstanceOf[Object])
        return true
      } else {
        ctx.removeAttribute(table.`var`, var_index)
      }
      return false
    }
  }

  class Col(context: ComponentContext) extends ClosingUIBean(context) {
    var property: String = _
    var title: String = _
    var width: String = _
    var row: Row = _
    var sortable: String = _
    var filterable: String = _

    override def start(writer: Writer): Boolean = {
      row = findAncestor(classOf[Row])
      if (row.index == 0) row.table.addCol(this)
      return null != row.curObj
    }

    override def doEnd(writer: Writer, body: String): Boolean = {
      if (context.theme.name == Themes.Default) {
        try {
          writer.append("<td").append(parameterString).append(">")
          if (Strings.isNotEmpty(body)) {
            writer.append(body)
          } else if (null != property) {
            val value = getValue()
            if (null != value) writer.append(StringUtil.XMLEncNA(value.toString()))
          }
          writer.append("</td>")
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
        return false
      } else {
        return super.doEnd(writer, body)
      }
    }

    /**
     * find value of row.obj's property
     */
    def getValue(): Any = getValue(row.curObj, property)

    def setTitle(title: String) {
      this.title = title
    }

    def getPropertyPath() = Strings.concat(row.table.`var`, ".", property)

    /**
     * 支持按照属性提取国际化英文名
     */
    def getTitle(): String = {
      if (null == title) {
        title = Strings.concat(row.table.`var`, ".", property)
      }
      return getText(title)
    }

    def getCurObj() = row.curObj

  }

  class Treecol(context: ComponentContext) extends Col(context) {
    override def doEnd(writer: Writer, body: String): Boolean = {
      this.body = body
      mergeTemplate(writer)
      false
    }

  }

  class Boxcol(context: ComponentContext) extends Col(context) {

    var `type` = "checkbox"

    // checkbox or radiobox name
    var boxname: String = _

    /** display or none */
    var display: Boolean = true

    var checked: Boolean = _

    override def start(writer: Writer): Boolean = {
      if (null == property) this.property = "id"
      row = findAncestor(classOf[Row])
      if (null == boxname) boxname = row.table.`var` + "." + property
      if (row.index == 0) row.table.addCol(this)
      return null != row.curObj
    }

    override def doEnd(writer: Writer, body: String): Boolean = {
      if (context.theme.name == Themes.Default) {
        try {
          writer.append("<td class=\"gridselect\"")
          if (null != id) writer.append(" id=\"").append(id).append("\"")
          writer.append(parameterString).append(">")
          if (display) {
            writer.append("<input class=\"box\" name=\"").append(boxname).append("\" value=\"")
              .append(String.valueOf(getValue())).append("\" type=\"").append(`type`).append("\"")
            if (checked) writer.append(" checked=\"checked\"")
            writer.append("/>")
          }
          if (Strings.isNotEmpty(body)) writer.append(body)
          writer.append("</td>")
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
        return false
      } else {
        return super.doEnd(writer, body)
      }
    }

    def getType() = `type`

    override def getTitle() = Strings.concat(row.table.`var`, "_", property)

    def getBoxname() = boxname

    def setBoxname(boxname: String) {
      this.boxname = boxname
    }

    def setType(`type`: String) {
      this.`type` = `type`
    }

    def isChecked() = checked

    def setChecked(checked: Boolean) {
      this.checked = checked
    }

    def isDisplay() = display

    def setDisplay(display: Boolean) {
      this.display = display
    }
  }
}

class Grid(context: ComponentContext) extends ClosingUIBean(context) {
  import Grid._
  val cols = new collection.mutable.ListBuffer[Col]
  val colTitles = new collection.mutable.HashSet[Object]
  var items: Object = _
  var caption: String = _
  var `var`: String = _
  var bar: String = _
  var sortable = "true"
  var filterable = "false"
  var filters = new collection.mutable.HashMap[String, String]

  /** 重新载入的时间间隔（以秒为单位） */
  var refresh: String = _

  /** 没有数据时显示的文本 */
  var emptyMsg: String = _

  def hasbar: Boolean = (null != bar || items.isInstanceOf[Page[_]])

  def pageable: Boolean = items.isInstanceOf[Page[_]]

  def notFullPage: Boolean = {
    if (items.isInstanceOf[Page[_]])
      items.asInstanceOf[Page[_]].size < items.asInstanceOf[Page[_]].pageSize
    else (items.asInstanceOf[Seq[_]]).isEmpty
  }

  def defaultSort(property: String) = Strings.concat(`var`, ".", property)

  def isSortable(cln: Col): Boolean = {
    val sortby = cln.parameters.get("sort").orNull
    if (null != sortby) true
    else ("true".equals(sortable) && !Objects.equals(cln.sortable, "false") && null != cln.property)
  }

  def isFilterable(cln: Col): Boolean = {
    ("true".equals(filterable) && !Objects.equals(cln.filterable, "false") && null != cln.property)
  }

  def addCol(column: Col) {
    var title = column.getTitle()
    if (null == title) title = column.property
    if (null == column.width && column.isInstanceOf[Boxcol]) column.width = "25px"
    if (!colTitles.contains(title)) {
      colTitles.add(title)
      cols += column
    }
  }

  override def evaluateParams(): Unit = {
    generateIdIfEmpty()
  }
}