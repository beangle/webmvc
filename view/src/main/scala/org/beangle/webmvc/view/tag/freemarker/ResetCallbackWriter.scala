/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.webmvc.view.tag.freemarker

import java.io.{ StringWriter, Writer }
import freemarker.template.TransformControl
import freemarker.template.TransformControl.{ END_EVALUATION, EVALUATE_BODY, REPEAT_EVALUATION, SKIP_BODY }
import org.beangle.webmvc.view.tag.Component

/**
 * ResetCallbackWriter
 *
 * @author chaostone
 * @since 2.4
 */
class ResetCallbackWriter extends Writer with TransformControl {
  private var bean: Component = _
  private var writer: Writer = _
  private var body: StringWriter = _
  private var _afterBody = false

  def this(bean: Component, writer: Writer) {
    this()
    this.bean = bean
    this.writer = writer
    if (bean.usesBody()) this.body = new StringWriter()
  }

  def close() {
    if (bean.usesBody()) body.close()
  }

  /**
   * let's just not do it (it will be flushed eventually anyway)
   */
  def flush() {
    // writer.flush()
  }

  def write(cbuf: Array[Char], off: Int, len: Int) {
    if (bean.usesBody() && !_afterBody) body.write(cbuf, off, len)
    else writer.write(cbuf, off, len)
  }

  def onStart(): Int = {
    bean.context.push(bean)
    return if (bean.start(this)) EVALUATE_BODY else SKIP_BODY
  }

  def afterBody(): Int = {
    _afterBody = true
    val repeat = bean.end(this, if (bean.usesBody()) body.toString() else "")
    if (repeat) {
      if (bean.usesBody()) {
        _afterBody = false
        body.getBuffer().delete(0, body.getBuffer().length())
      }
      return REPEAT_EVALUATION
    } else {
      bean.context.pop()
      return END_EVALUATION
    }
  }

  def onError(throwable: Throwable) {
    throw throwable
  }

  def getBean() = bean

}
