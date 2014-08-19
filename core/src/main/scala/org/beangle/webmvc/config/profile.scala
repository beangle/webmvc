package org.beangle.webmvc.config

import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings.{ contains, isEmpty, split, substringBeforeLast, uncapitalize }
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.execution.Interceptor

object Constants {
  // 路径分割符
  val separator = '/'

  val SHORT_URI = "short"

  val SEO_URI = "seo"

  val SIMPLE_URI = "simple"

  val FULL_VIEWPATH = "full"

  val SEO_VIEWPATH = "seo"

  val SIMPLE_VIEWPATH = "simple"
}

object Profile extends Logging {

  def getMatchInfo(className: String, pattens: Array[String]): Option[MatchInfo] = {
    var sub = className
    var index = 0
    val reserved = new StringBuffer
    var i = 0
    while (i < pattens.length) {
      var subIndex = sub.indexOf(pattens(i))
      if (-1 == subIndex) return None

      // 串接所有匹配项保留部分
      if (0 != subIndex) {
        if (reserved.length > 0) reserved.append('.')
        reserved.append(sub.substring(0, subIndex))
      }
      index += (subIndex + pattens(i).length)
      if (i != pattens.length - 1) {
        sub = sub.substring(subIndex + pattens(i).length)
        if (isEmpty(sub)) {
          return Some(new MatchInfo(className.length - 1, reserved.toString))
        }
      }
      i += 1
    }
    Some(new MatchInfo(index - 1, reserved.toString))
  }
}

/**
 * name: 配置名
 * actionPattern :action所在的包,匹配action的唯一条件
 */
final class Profile(val name: String, val actionPattern: String) extends Comparable[Profile] {

  // action类名后缀
  var actionSuffix: String = _

  // 缺省的action中的方法
  var defaultMethod = "index"

  // 路径前缀
  var viewPath: String = _

  // 路径模式
  var viewPathStyle = "simple"

  // 路径后缀
  var viewSuffix: String = _

  // URI ROOT
  var uriPath = "/"

  // URI style
  var uriPathStyle = "simple"

  /** URI的后缀 */
  var uriSuffix: String = _

  var interceptors: Array[Interceptor] = Array()
  
  // 匹配缓存[className,matchInfo]
  private val matchInfos = new collection.mutable.HashMap[String, MatchInfo]

  /**
   * 得到控制器的起始位置
   */
  def matches(className: String): Option[MatchInfo] = {
    var matchInfo = matchInfos.get(className)
    if (matchInfo.isEmpty) {
      if (className.endsWith(actionSuffix)) {
        val newMatchInfo = Profile.getMatchInfo(className, split(actionPattern, '*'))
        if (!newMatchInfo.isEmpty) {
          matchInfos.put(className, newMatchInfo.get)
          matchInfo = newMatchInfo
        }
      } else None
    }
    matchInfo
  }
  /**
   * 子包优先
   */
  override def compareTo(other: Profile): Int = {
    other.actionPattern.compareTo(this.actionPattern)
  }

  /**
   * 取得类对应的全路经，仅仅把类名第一个字母小写。
   */
  def getFullPath(className: String): String = {
    val postfix = actionSuffix
    val simpleName = {
      val simpleName = className.substring(className.lastIndexOf('.') + 1)
      if (contains(simpleName, postfix)) {
        uncapitalize(simpleName.substring(0, simpleName.length - postfix.length))
      } else {
        uncapitalize(simpleName)
      }
    }

    val infix = new StringBuilder
    infix.append(substringBeforeLast(className, "."))
    if (infix.length == 0) return simpleName
    infix.append('.')
    infix.append(simpleName)
    // 将.替换成/
    for (i <- 0 until infix.length if (infix.charAt(i) == '.')) infix.setCharAt(i, '/')

    infix.toString
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

    val matchInfo = matches(className).get
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
    infix.toString
  }

  override def toString: String = {
    Objects.toStringBuilder(this).add("name", name).add("actionPattern", actionPattern)
      .add("actionSuffix", actionSuffix).add("viewPath", viewPath)
      .add("viewPathStyle", viewPathStyle).add("viewSuffix", viewSuffix).add("uriPath", uriPath)
      .add("uriPathStyle", uriPathStyle).add("uriSuffix", uriSuffix)
      .add("defaultMethod", defaultMethod).toString
  }
}

/**
 * action匹配信息
 */
class MatchInfo(val start: Int, val reserved: String) {
  override def toString: String = reserved
}

