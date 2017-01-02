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
package org.beangle.webmvc.view.tag

import java.io.Writer

class Component(val context: ComponentContext) {

  val parameters = new collection.mutable.LinkedHashMap[String, Object]
  var theme: String = _
  /**
   * Callback for the start tag of this component. Should the body be  evaluated?
   */
  def start(writer: Writer) = true

  /**
   * Callback for the end tag of this component. Should the body be evaluated again?
   */
  def end(writer: Writer, body: String): Boolean = {
    writer.write(body)
    false
  }

  /**
   * Finds the nearest ancestor of this component stack.
   *
   * @param clazz the class to look for, or if assignable from.
   * @return the component if found, <tt>null</tt> if not.
   */
  protected final def findAncestor[T <: Component](clazz: Class[T]): T = {
    context.find(clazz)
  }

  /**
   * Adds the given key and value to this component's own parameter.
   * <p/>
   * If the provided key is <tt>null</tt> nothing happens. If the provided value is <tt>null</tt>
   * any existing parameter with the given key name is removed.
   */
  final def addParameter(key: String, value: Object): Unit = {
    if (key != null) {
      if (value == null) parameters.remove(key)
      else parameters.put(key, value)
    }
  }
  /**
   * 将所有额外参数链接起来
   *
   * @return 空格开始 空格相隔的参数字符串
   */
  final def parameterString: String = {
    val sb = new StringBuilder(parameters.size * 10)
    for ((key, value) <- parameters) {
      sb.append(" ").append(key).append("=\"").append(value.toString).append("\"")
    }
    sb.toString
  }

  def evaluateParams(): Unit = {}
  /**
   * Overwrite to set if body shold be used.
   * @return always false for this component.
   */
  def usesBody(): Boolean = false
}