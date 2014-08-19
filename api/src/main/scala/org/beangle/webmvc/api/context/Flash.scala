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
    getActionMessages(next, message).messages += message
  }

  /**
   * 添加错误消息到下一次请求
   */
  def addError(error: String) {
    getActionMessages(next, error).errors += error
  }

  /**
   * 添加消息到本次请求
   */
  def addMessageNow(message: String) {
    getActionMessages(now, message).messages += message
  }

  /**
   * 添加错误到本次请求
   */
  def addErrorNow(message: String): Unit = {
    getActionMessages(now, message).errors += message
  }

  private def getActionMessages(map: Map[Object, Object], message: String): ActionMessages = {
    map.get(Flash.MESSAGES) match {
      case messages: ActionMessages => messages
      case _ => {
        val messages = new ActionMessages()
        now.put(Flash.MESSAGES, messages)
        messages
      }
    }
  }
}