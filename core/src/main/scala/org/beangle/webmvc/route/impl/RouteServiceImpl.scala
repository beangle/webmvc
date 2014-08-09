package org.beangle.webmvc.route.impl

import java.net.URL
import java.{ util => ju, lang => jl }
import org.beangle.commons.bean.PropertyUtils.{ copyProperty, getProperty }
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.RequestUtils
import org.beangle.webmvc.route.{ Action, ActionMapping, Handler, Profile, RequestMapper, RouteService }
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.context.ActionContext

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
      populateAttr(profile, "viewSuffix", props)
      populateAttr(profile, "viewPathStyle", props)
      populateAttr(profile, "defaultMethod", props)
      populateAttr(profile, "uriPath", props)
      populateAttr(profile, "uriPathStyle", props)
      populateAttr(profile, "uriSuffix", props)
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
  def buildAction(clazz: Class[_], method: String = null): Action = {
    actionBuilder.build(clazz, method)
  }

  def buildActions(clazz: Class[_]): Seq[Action] = {
    actionBuilder.build(clazz)
  }
  /**
   * viewname -> 页面路径的映射
   */
  def mapView(className: String, viewName: String): String = {
    viewMapper.map(className, viewName)
  }
}

class HierarchicalUrlMapper extends RequestMapper {
  private val mappings = new ActionMappings

  val DefaultMethod = "index"
  val MethodParam = "_method"

  def add(mapping: ActionMapping): Unit = {
    mappings.add(mapping)
  }

  def resolve(uri: String): Option[ActionMapping] = {
    mappings.resolve(uri)
  }

  def resolve(request: HttpServletRequest): Option[ActionMapping] = {
    val uri = RequestUtils.getServletPath(request)
    var bangIdx, dotIdx = -1
    val lastSlashIdx = uri.lastIndexOf('/')
    val sb = new jl.StringBuilder(uri.length + 10)
    if (lastSlashIdx == uri.length - 1) {
      sb.append(uri).append(determineMethod(request, DefaultMethod))
    } else {
      var i = lastSlashIdx + 2
      var chars = new Array[Char](uri.length)
      uri.getChars(0, chars.length, chars, 0)
      while (i < chars.length && dotIdx == -1) {
        var c = chars(i)
        if ('!' == c) bangIdx = i
        else if ('.' == c) dotIdx = i
        i += 1
      }
      sb.append(chars)
      if (dotIdx > 0) sb.delete(dotIdx, sb.length)
      if (bangIdx > 0) sb.setCharAt(bangIdx, '/')
      else {
        val method = determineMethod(request, null)
        if (null != method && -1 == sb.indexOf(method, lastSlashIdx + 1)) sb.append('/').append(method)
      }
    }
    mappings.resolve(sb.toString)
  }

  private def determineMethod(request: HttpServletRequest, defaultMethod: String): String = {
    val method = request.getParameter(MethodParam)
    if (null == method) defaultMethod else method
  }
}

class ActionMappings {
  val children = new collection.mutable.HashMap[String, ActionMappings]
  val mappings = new collection.mutable.HashMap[String, ActionMapping]

  def add(mapping: ActionMapping): Unit = {
    if (mapping.isPattern) add(mapping.url, mapping, this)
    else mappings.put(mapping.url, mapping)
  }

  def add(pattern: String, mapping: ActionMapping, mappings: ActionMappings): Unit = {
    val slashIndex = pattern.indexOf('/', 1)
    val head = if (-1 == slashIndex) pattern.substring(1) else pattern.substring(1, slashIndex)
    val headPattern = ActionMapping.matcherName(head)

    if (-1 == slashIndex) {
      mappings.mappings.put(headPattern, mapping)
    } else {
      add(pattern.substring(slashIndex), mapping, mappings.children.getOrElseUpdate(headPattern, new ActionMappings))
    }
  }

  def resolve(uri: String): Option[ActionMapping] = {
    val directMapping = mappings.get(uri)
    if (None != directMapping) return directMapping

    val parts = Strings.split(uri, '/')
    find(0, parts, this) match {
      case Some(m) =>
        if (m.isPattern) {
          val urlParams = new collection.mutable.HashMap[String, String]
          m.params(ActionContext.URLParams).asInstanceOf[Map[Integer, String]] foreach {
            case (k, v) =>
              urlParams.put(v, parts(k))
          }
          Some(ActionMapping(m.url, m.handler, m.namespace,m.name,m.params ++ urlParams))
        } else Some(m)
      case None => None
    }
  }

  def find(index: Int, parts: Array[String], mappings: ActionMappings): Option[ActionMapping] = {
    if (index < parts.length && null != mappings) {
      if (index == parts.length - 1) {
        val mapping = mappings.mappings.get(parts(index))
        if (mapping == None) mappings.mappings.get("*") else mapping
      } else {
        val mapping = find(index + 1, parts, mappings.children.get(parts(index)).orNull)
        if (mapping == None) find(index + 1, parts, mappings.children.get("*").orNull)
        else mapping
      }
    } else {
      None
    }
  }
}
