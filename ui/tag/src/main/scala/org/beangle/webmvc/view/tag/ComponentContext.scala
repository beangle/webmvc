package org.beangle.webmvc.view.tag

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.view.impl.UIIdGenerator

class ComponentContext(val uriRender: ActionUriRender, val idGenerator: UIIdGenerator, val templateEngine: TemplateEngine) {

  private val themeStack = new ThemeStack()

  private val components = new collection.mutable.Stack[Component]

  /**
   * Finds the nearest ancestor of this component stack.
   */
  def find[T <: Component](clazz: Class[T]): T = {
    components.find { component => clazz == component.getClass } match {
      case Some(c) => c.asInstanceOf[T]
      case None => throw new RuntimeException(s"Cannot find ancestor of type ${clazz.getName}")
    }
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