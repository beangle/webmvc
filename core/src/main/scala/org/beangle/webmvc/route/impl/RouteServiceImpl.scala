package org.beangle.webmvc.route.impl

import java.net.URL
import java.{ util => ju }
import org.beangle.commons.bean.PropertyUtils.{ copyProperty, getProperty }
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.route.{ Action, Profile, RouteService, RequestMapper, ActionMapping }
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.web.util.RequestUtils

object RouteServiceImpl extends Logging {

  val defaultProfile = loadDefaultProfile()

  /**
   * 初始化配置META-INF/convention-route.properties
   */
  def loadProfiles(): List[Profile] = {
    val profiles = new collection.mutable.ListBuffer[Profile]
    ClassLoaders.getResources("META-INF/beangle/convention-route.properties").foreach { url =>
      profiles ++= buildProfiles(url, false)
    }
    profiles.toList
  }

  /**加载META-INF/convention-default.properties*/
  private def loadDefaultProfile(): Profile = {
    val convention_default = ClassLoaders.getResource("META-INF/beangle/convention-default.properties")
    if (null == convention_default) { throw new RuntimeException("cannot find convention-default.properties!") }
    buildProfiles(convention_default, true)(0)
  }

  private def buildProfiles(url: URL, isDefault: Boolean): Seq[Profile] = {
    val myProfiles = new collection.mutable.ListBuffer[Profile]
    val props = IOs.readJavaProperties(url)
    if (isDefault) {
      val profile = populatProfile(props, "default")
      myProfiles += profile
    } else {
      var profileIndex: Int = 0
      var ifBreak = true
      while (ifBreak) {
        var profile = populatProfile(props, "profile" + profileIndex)
        if (null == profile) {
          ifBreak = false
        } else {
          myProfiles += profile
        }
        profileIndex += 1
      }
    }
    myProfiles
  }

  private def populatProfile(props: Map[String, String], name: String): Profile = {
    val actionPattern = props.get(name + ".actionPattern").orNull
    if (Strings.isEmpty(actionPattern)) null
    else {
      val profile = new Profile(name, actionPattern)
      populateAttr(profile, "actionSuffix", props)
      populateAttr(profile, "viewPath", props)
      populateAttr(profile, "viewExtension", props)
      populateAttr(profile, "viewPathStyle", props)
      populateAttr(profile, "defaultMethod", props)
      populateAttr(profile, "uriPath", props)
      populateAttr(profile, "uriPathStyle", props)
      populateAttr(profile, "uriExtension", props)
      populateAttr(profile, "actionScan", props)
      profile
    }
  }

  private def populateAttr(profile: Profile, attr: String, props: Map[String, String]) {
    props.get(profile.name + "." + attr) match {
      case Some(v) => if (Strings.isBlank(v)) copyProperty(profile, attr, null) else copyProperty(profile, attr, v)
      case None => copyProperty(profile, attr, getProperty(defaultProfile, attr))
    }
  }
}

class RouteServiceImpl extends RouteService with Logging {

  val viewMapper = new DefaultViewMapper(this)
  val actionBuilder = new DefaultActionBuilder(this)

  val profiles: List[Profile] = RouteServiceImpl.loadProfiles

  // 匹配缓存[String,Profile]
  private val cache = new ju.concurrent.ConcurrentHashMap[String, Profile]

  def getProfile(className: String): Profile = {
    var matched = cache.get(className)
    if (null != matched) { return matched }
    var index: Int = -1
    var patternLen: Int = 0
    for (profile <- profiles if (profile.isMatch(className))) {
      var newIndex = profile.matchedIndex(className)
      if (newIndex >= index && profile.actionPattern.length >= patternLen) {
        matched = profile
        index = newIndex
        patternLen = profile.actionPattern.length
      }
    }
    if (matched == null) {
      matched = RouteServiceImpl.defaultProfile
    }
    cache.put(className, matched)
    debug(s"${className} match profile:${matched}")
    matched
  }

  def getProfile(clazz: Class[_]): Profile = {
    getProfile(clazz.getName())
  }
  /**
   * 默认类名对应的控制器名称(含有扩展名)
   */
  def buildAction(className: String): Action = {
    actionBuilder.build(className)
  }

  /**
   * viewname -> 页面路径的映射
   */
  def mapView(className: String, methodName: String, viewName: String): String = {
    viewMapper.getViewPath(className, methodName, viewName)
  }
}

//FIXME support restfull url
class DefaultURIResolver extends RequestMapper {
  val DefaultMethod = "index"
  val MethodParam = "_method"

  def resolve(request: HttpServletRequest): ActionMapping = {
    val uri = RequestUtils.getServletPath(request)
    val lastSlash = uri.lastIndexOf("/")
    val data =
      if (lastSlash == -1) {
        ("", uri)
      } else if (lastSlash == 0) {
        ("/", uri.substring(lastSlash + 1))
      } else {
        // Simply select the namespace as everything before the last slash
        (uri.substring(0, lastSlash), uri.substring(lastSlash + 1))
      }
    val namespace = data._1
    val name = data._2

    // process ! . 
    var i = 0
    var bangIdx = -1
    var lastIdx = name.length
    val chars = new Array[Char](name.length)
    name.getChars(0, name.length, chars, 0)
    var continue = true
    while (i < chars.length && continue) {
      var c = chars(i)
      if ('!' == c) bangIdx = i
      else if (';' == c || '.' == c) {
        lastIdx = i
        continue = false
      }
      i += 1
    }

    if (-1 == bangIdx) {
      ActionMapping(namespace, name.substring(0, lastIdx), determinMethod(request))
    } else {
      ActionMapping(namespace, name.substring(0, bangIdx), name.substring(bangIdx + 1, lastIdx))
    }
  }

  private def determinMethod(request: HttpServletRequest): String = {
    val method = request.getParameter(MethodParam)
    if (null == method) DefaultMethod else method
  }
}