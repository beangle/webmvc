package org.beangle.webmvc.view.component

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
    context.findAncestor(clazz)
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
      sb.append(" ").append(if ("cssClass".equals(key)) "class" else key).append("=\"").append(value.toString()).append("\"")
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