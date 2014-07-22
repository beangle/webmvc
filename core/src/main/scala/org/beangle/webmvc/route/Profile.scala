package org.beangle.webmvc.route

import java.{util => ju}
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.beangle.commons.logging.Logging

object Profile extends Logging {

  def getMatchInfo(pattens: Array[String], className: String): MatchInfo = {
    var sub = className
    var index = 0
    val matchInfo = new MatchInfo(-1)
    var i = 0
    while (i < pattens.length) {
      var subIndex = sub.indexOf(pattens(i))
      if (-1 == subIndex) return matchInfo

      // 串接所有匹配项保留部分
      if (0 != subIndex) {
        if (matchInfo.reserved.length > 0) matchInfo.reserved.append('.')
        matchInfo.reserved.append(sub.substring(0, subIndex))
      }
      index += (subIndex + pattens(i).length)
      if (i != pattens.length - 1) {
        sub = sub.substring(subIndex + pattens(i).length())
        if (isEmpty(sub)) {
          matchInfo.start = className.length() - 1
          return matchInfo
        }
      }
      i += 1
    }
    matchInfo.start = index - 1
    matchInfo
  }

  def isInPackage(packageName: String, className: String): Boolean = getMatchInfo(split(packageName, '*'), className).start != -1

}
/**
 * 路由调转配置
 *
 * @author chaostone <br>
 *         /:controller:ext =>:method=index||get("method")
 *         /:controller/:method:ext
 */
import Profile._
/**
 * name: 配置名
 * actionPattern :action所在的包,匹配action的唯一条件
 */
final class Profile(val name: String, val actionPattern: String) extends Comparable[Profile] {

  val patternSegs: Array[String] = split(actionPattern, '*')

  // action类名后缀
  var actionSuffix: String = _

  // 扫描action
  var actionScan: Boolean = _

  // 路径前缀
  var viewPath: String = _

  // 路径模式
  var viewPathStyle = "simple"

  // 路径后缀
  var viewExtension: String = _

  // 缺省的action中的方法
  var defaultMethod = "index"

  // URI ROOT
  var uriPath = "/"

  // URI style
  var uriPathStyle = "simple"

  /** URI的后缀 */
  var uriExtension: String = _

  // 匹配缓存[className,matchInfo]
  private var cache = new ju.concurrent.ConcurrentHashMap[String, MatchInfo]

  /**
   * 得到控制器的起始位置
   */
  def getCtlMatchInfo(className: String): MatchInfo = {
    cache.get(className) match {
      case matchInfo: MatchInfo => matchInfo
      case _ => {
        val matchInfo = getMatchInfo(patternSegs, className)
        if (-1 != matchInfo.start) cache.put(className, matchInfo)
        matchInfo
      }
    }
  }

  /**
   * 给定action是否符合该配置文件
   */
  def isMatch(className: String): Boolean = getMatchInfo(patternSegs, className).start != -1

  def matchedIndex(className: String): Int = getMatchInfo(patternSegs, className).start

  /**
   * 子包优先
   */
  override def compareTo(other: Profile): Int = other.actionPattern.compareTo(this.actionPattern)

  /**
   * 取得类名称对应的全路经，仅仅把类名第一个字母小写。
   */
  def getFullPath(className: String): String = {
    val postfix = actionSuffix
    val simpleName = {
      val simpleName = className.substring(className.lastIndexOf('.') + 1)
      if (contains(simpleName, postfix)) {
        uncapitalize(simpleName.substring(0, simpleName.length() - postfix.length()))
      } else {
        uncapitalize(simpleName)
      }
    }

    val infix = new StringBuilder()
    infix.append(substringBeforeLast(className, "."))
    if (infix.length() == 0) return simpleName
    infix.append('.')
    infix.append(simpleName)
    // 将.替换成/
    for (i <- 0 until infix.length() if (infix.charAt(i) == '.')) infix.setCharAt(i, '/')

    infix.toString()
  }

  /**
   * 将前后缀去除后，中间的.替换为/<br>
   * 不以/开始。
   */
  def getInfix(className: String): String = {
    val postfix = actionSuffix
    val simpleName = {
      if (className.endsWith(postfix)) {
        uncapitalize(className.substring(className.lastIndexOf('.') + 1, className.length - postfix.length))
      } else {
        uncapitalize(className.substring(className.lastIndexOf('.') + 1))
      }
    }

    val matchInfo = getCtlMatchInfo(className)
    val infix = new StringBuilder(matchInfo.reserved.toString)
    if (infix.length > 0) infix.append('.')

    val remainder = {
      val remainder = substringBeforeLast(className, ".").substring(matchInfo.start + 1)
      if (remainder.length > 0) {
        if ('.' == remainder.charAt(0)) remainder.substring(1) else remainder
      } else remainder
    }
    if (remainder != "") infix.append(remainder).append('.')

    if (infix.length == 0) return simpleName
    infix.append(simpleName)

    // 将.替换成/
    for (i <- 0 until infix.length if (infix.charAt(i) == '.')) infix.setCharAt(i, '/')
    infix.toString()
  }

  override def toString(): String = Objects.toStringBuilder(this).add("name", name).add("actionPattern", actionPattern)
    .add("actionSuffix", actionSuffix).add("actionScan", actionScan.toString).add("viewPath", viewPath)
    .add("viewPathStyle", viewPathStyle).add("viewExtension", viewExtension).add("uriPath", uriPath)
    .add("uriPathStyle", uriPathStyle).add("uriExtension", uriExtension)
    .add("defaultMethod", defaultMethod).toString()
}

/**
 * action匹配信息
 */
class MatchInfo( var start: Int = -1) {

  var reserved = new StringBuilder(0)

  override def toString(): String = reserved.toString()
}
