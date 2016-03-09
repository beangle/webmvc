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
package org.beangle.webmvc.api.context

import java.util.Collection
import java.util.Map
import java.util.Set
import java.util.HashMap

object Flash {

  val MESSAGES = "messages"
}

@SerialVersionUID(-5292283953338410228L)
class Flash extends Map[Object, Object] with Serializable {
  /**
   * current request
   */
  val now: Map[Object, Object] = new HashMap()

  /**
   * next request
   */
  val next: Map[Object, Object] = new HashMap()

  /**
   * return now and session saved
   */
  def keySet(): Set[Object] = now.keySet()

  /**
   * return now and session saved value
   */
  def get(key: Object): Object = now.get(key)

  /**
   * put value to next
   */
  def put(key: Object, value: Object): Object = next.put(key, value)

  def putAll(values: Map[_ <: Object, _ <: Object]) {
    next.putAll(values)
  }

  def keep(key: String) {
    next.put(key, now.get(key))
  }

  def keep() {
    next.putAll(now)
  }

  def nextToNow() {
    if (now.isEmpty && next.isEmpty) return
    now.clear()
    now.putAll(next)
    next.clear()
  }

  def clear() {
    now.clear()
  }

  def containsKey(key: Object): Boolean = now.containsKey(key)

  def containsValue(value: Object): Boolean = now.containsValue(value)

  def entrySet(): Set[Map.Entry[Object, Object]] = now.entrySet()

  def isEmpty(): Boolean = now.isEmpty()

  def remove(key: Object): Object = now.remove(key)

  def size(): Int = now.size()

  def values(): Collection[Object] = now.values()

  /**
   * 添加消息到下一次请求
   */
  def addMessage(message: String) {
    getActionMessages(next).messages += message
  }

  /**
   * 添加错误消息到下一次请求
   */
  def addError(error: String) {
    getActionMessages(next).errors += error
  }

  /**
   * 添加消息到本次请求
   */
  def addMessageNow(message: String) {
    getActionMessages(now).messages += message
  }

  /**
   * 添加错误到本次请求
   */
  def addErrorNow(message: String): Unit = {
    getActionMessages(now).errors += message
  }

  private def getActionMessages(map: Map[Object, Object]): ActionMessages = {
    map.get(Flash.MESSAGES) match {
      case messages: ActionMessages => messages
      case _ => {
        val messages = new ActionMessages()
        map.put(Flash.MESSAGES, messages)
        messages
      }
    }
  }
}