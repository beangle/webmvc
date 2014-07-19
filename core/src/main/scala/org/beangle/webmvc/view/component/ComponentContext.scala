package org.beangle.webmvc.view.component

import scala.Range

import org.beangle.webmvc.view.bean.{ ActionUriRender, UIIdGenerator }
import org.beangle.webmvc.view.template.{ TemplateEngine, Theme, ThemeStack, Themes }

class ComponentContext(val uriRender: ActionUriRender, val idGenerator: UIIdGenerator, val templateEngine: TemplateEngine) {

  private val themeStack = new ThemeStack()

  private val components = new collection.mutable.Stack[Component]

  /**
   * Finds the nearest ancestor of this component stack.
   * FIXME for scala stack list
   * @param clazz the class to look for, or if assignable from.
   * @return the component if found, <tt>null</tt> if not.
   */
  def findAncestor[T <: Component](clazz: Class[T]): T = {
    val size = components.size
    Range(0, size - 1) foreach { i =>
      val component = components(size - i - 2)
      if (clazz == component.getClass()) return component.asInstanceOf[T]
    }
    return null.asInstanceOf[T]
  }

  def pop(): Component = {
    val elem = components.pop()
    if (null != elem.theme) themeStack.pop()
    elem
  }

  def theme: Theme = {
    if (themeStack.isEmpty) Themes.Default
    else themeStack.peek
  }

  def push(component: Component): Unit = {
    components.push(component)
    if (null != component.theme) themeStack.push(Themes(component.theme))
  }
}