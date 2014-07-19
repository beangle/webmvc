package org.beangle.webmvc.view.template

import java.util.Stack

/**
 * ui主体栈
 *
 * @author chaostone
 * @version $Id: ThemeStack.java Jul 28, 2011 12:04:52 PM chaostone $
 */
class ThemeStack {
  private val themes = new Stack[Theme]()

  def push(item: Theme): Theme = themes.push(item)

  def pop(): Theme = themes.pop()

  def peek(): Theme = themes.peek()

  def isEmpty(): Boolean = themes.isEmpty()

}