package org.beangle.webmvc.config

import java.net.URL

import scala.Range

import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings.{ isEmpty, split, uncapitalize }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.intercept.Interceptor

object Profile extends Logging {

  val / = '/'

  val SHORT_URI = "short"

  val SEO_URI = "seo"

  val SIMPLE_URI = "simple"

  val FULL_VIEWPATH = "full"

  val SEO_VIEWPATH = "seo"

  val SIMPLE_VIEWPATH = "simple"

  def matches(className: String, actionSuffix: String, actionPattern: String): Option[String] = {
    val reserved = new StringBuffer
    val pattens = split(actionPattern, '*')
    var sub = className
    var index = 0
    var i = 0
    while (i < pattens.length) {
      val pattern = pattens(i)
      val subIndex = sub.indexOf(pattern)
      if (-1 == subIndex) return None

      // 串接所有匹配项保留部分
      if (0 != subIndex) {
        if (reserved.length > 0) reserved.append('.')
        reserved.append(sub.substring(0, subIndex))
      }
      index += (subIndex + pattern.length)
      if (i != pattens.length - 1) {
        sub = sub.substring(subIndex + pattern.length)
        if (isEmpty(sub)) return Some(getInfix(className, actionSuffix, className.length - 1, reserved.toString))
      }
      i += 1
    }
    Some(getInfix(className, actionSuffix, index - 1, reserved.toString))
  }

  /**
   * 将前后缀去除后，中间的.替换为/<br>
   * 不以/开始。
   */
  private def getInfix(className: String, postfix: String, startIndex: Int, reserved: String): String = {
    val afterLastDotIdx = className.lastIndexOf('.') + 1
    val simpleName = uncapitalize(className.substring(afterLastDotIdx, className.length - postfix.length))

    val infix = new StringBuilder(reserved)
    if (infix.length > 0) infix.append('.')

    if (startIndex + 2 < afterLastDotIdx) infix.append(className.substring(startIndex + 2, afterLastDotIdx))

    if (infix.length == 0) return simpleName
    infix.append(simpleName)
    Range(0, infix.length) foreach { i =>
      if (infix.charAt(i) == '.') infix.setCharAt(i, '/')
    }
    infix.toString
  }
}

/**
 * name: 配置名
 * actionPattern :action所在的包,匹配action的唯一条件
 */
final class Profile(val name: String,
  val actionPattern: String,
  val actionSuffix: String,
  val defaultMethod: String,
  val viewPath: String,
  val viewPathStyle: String,
  val viewSuffix: String,
  val viewType: String,
  val urlPath: String,
  val urlStyle: String,
  val urlSuffix: String,
  val interceptorNames: Array[String],
  val interceptors: Array[Interceptor],
  val source: URL) extends Comparable[Profile] {

  import Profile._

  // 匹配缓存[className,matched_info]
  private val matched = new collection.mutable.HashMap[String, String]
  /**
   * 得到控制器的起始位置
   */
  def matches(className: String): Option[String] = {
    var result = matched.get(className)
    if (result.isEmpty) {
      if (className.endsWith(actionSuffix)) {
        val newMatchInfo = Profile.matches(className, this.actionSuffix, actionPattern)
        if (!newMatchInfo.isEmpty) {
          matched.put(className, newMatchInfo.get)
          result = newMatchInfo
        }
      } else None
    }
    result
  }

  def getMatched(className: String): String = {
    matched(className)
  }
  /**
   * 子包优先
   * first com.beangle.aa.bb.web.action then com.beangle.*.web.action
   */
  override def compareTo(other: Profile): Int = {
    other.actionPattern.compareTo(this.actionPattern)
  }

  override def toString: String = {
    Objects.toStringBuilder(this).add("name", name).add("actionPattern", actionPattern)
      .add("actionSuffix", actionSuffix).add("viewPath", viewPath)
      .add("viewPathStyle", viewPathStyle).add("viewSuffix", viewSuffix)
      .add("viewType", viewType).add("urlPath", urlPath)
      .add("urlStyle", urlStyle).add("urlSuffix", urlSuffix)
      .add("defaultMethod", defaultMethod).toString
  }
}

final class ProfileConfig(val name: String, val actionPattern: String) {

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

  // View Type (freemarker chain)
  var viewType: String = _

  //end with /
  var urlPath = "/"

  // URI style
  var urlStyle = "simple"

  /** URL的后缀 */
  var urlSuffix: String = _

  var interceptorNames: Array[String] = Array()

  var source: URL = _

  def mkProfile(interceptors: Array[Interceptor]): Profile = {
    new Profile(name, actionPattern, actionSuffix, defaultMethod, viewPath, viewPathStyle, viewSuffix, viewType, urlPath, urlStyle, urlSuffix, interceptorNames, interceptors, source)
  }
}

