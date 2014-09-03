package org.beangle.webmvc.view.tag

import java.util.Stack

/**
 * ui主体栈
 *
 * @author chaostone
 */
class ThemeStack {
  private val themes = new Stack[Theme]()

  def push(item: Theme): Theme = themes.push(item)

  def pop(): Theme = themes.pop()

  def peek(): Theme = themes.peek()

  def isEmpty(): Boolean = themes.isEmpty()

}