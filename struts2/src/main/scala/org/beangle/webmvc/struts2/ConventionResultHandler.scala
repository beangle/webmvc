package org.beangle.webmvc.struts2

import scala.collection.JavaConversions.{ asScalaSet, mapAsJavaMap, mapAsScalaMap }

import org.apache.struts2.ServletActionContext
import org.apache.struts2.dispatcher.ServletRedirectResult
import org.apache.struts2.views.freemarker.FreemarkerManager
import org.beangle.commons.lang.Strings.{ contains, isEmpty, isNotEmpty, substringAfter, substringBefore }
import org.beangle.commons.web.util.RequestUtils.getServletPath
import org.beangle.webmvc.api.action.{ To, ToClass, ToStruts, ToURI, to }
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.dispatch.{ RequestMapper, RequestMapping }
import org.beangle.webmvc.view.ViewMapper
import org.beangle.webmvc.view.freemarker.TemplateFinderByConfig
import org.beangle.webmvc.view.impl.DefaultViewMapper

import com.opensymphony.xwork2.{ ActionContext, ObjectFactory, Result, UnknownHandler, XWorkException }
import com.opensymphony.xwork2.config.Configuration
import com.opensymphony.xwork2.config.entities.{ ActionConfig, ResultConfig, ResultTypeConfig }
import com.opensymphony.xwork2.inject.Inject

import javax.servlet.http.HttpServletRequest

/**
 * 实现action到result之间的路由和处理<br>
 * 默认按照方法进行路由
 *
 * @author chaostone
 */
class ConventionResultHandler extends UnknownHandler {

  private var resultTypeConfigs: Map[String, ResultTypeConfig] = Map.empty

  private var templateFinder: TemplateFinderByConfig = _

  @Inject
  var objectFactory: ObjectFactory = _

  @Inject
  var resolver: RequestMapper = _

  var viewMapper: ViewMapper = _

  var configurer: Configurer = _

  @Inject
  def this(configuration: Configuration, freemarkerManager: FreemarkerManager, configurer: Configurer, viewMapper: ViewMapper) = {
    this()
    this.configurer = configurer
    this.viewMapper = viewMapper
    this.templateFinder = new TemplateFinderByConfig(freemarkerManager.getConfig(), viewMapper, configurer)
    val types = Map(("freemarker", ".ftl"), ("velocity", ".vm"), ("dispatcher", ".jsp"))
    val pc = configuration.getPackageConfig("struts-default")
    val resTypeConfigs = new collection.mutable.HashMap[String, ResultTypeConfig]
    import scala.collection.JavaConversions.asScalaSet
    for (name <- pc.getAllResultTypeConfigs().keySet()) {
      val rtc = pc.getAllResultTypeConfigs().get(name)
      types.get(name).foreach { suffix => resTypeConfigs.put(suffix, rtc) }
      resTypeConfigs.put(name, rtc)
    }
    this.resultTypeConfigs = resTypeConfigs.toMap
  }

  @throws(classOf[XWorkException])
  override def handleUnknownResult(context: ActionContext, actionName: String, actionConfig: ActionConfig, resultCode: String): Result = {
    var path: String = null
    var cfg: ResultTypeConfig = null
    var newResultCode = resultCode
    // first route by common result
    if (!contains(newResultCode, ':')) {
      val actionClass = context.getActionInvocation().getProxy().getAction().getClass
      val className = actionClass.getName()
      val methodName = context.getActionInvocation().getProxy().getMethod()
      if (isEmpty(newResultCode)) newResultCode = "index"
      val viewName = DefaultViewMapper.defaultView(methodName, newResultCode)
      val suffix = configurer.getProfile(className).viewSuffix
      if (".ftl" == suffix) path = templateFinder.find(actionClass, viewName, suffix)
      if (null == path) {
        val buf = new StringBuilder()
        buf.append(viewMapper.map(className, DefaultViewMapper.defaultView(methodName, newResultCode), configurer.getProfile(className)))
        buf.append(suffix)
        path = buf.toString
      }
      cfg = resultTypeConfigs(suffix)
      return buildResult(newResultCode, cfg, context, buildResultParams(path, cfg))
    } else {
      // by prefix
      val prefix = substringBefore(newResultCode, ":")
      cfg = resultTypeConfigs(prefix)
      if (prefix.startsWith("chain")) {
        val action = buildAction(substringAfter(newResultCode, ":"), true)
        val params = buildResultParams(path, cfg)
        addNamespaceAction(action, params)
        if (isNotEmpty(action.method)) params.put("method", action.method)

        buildResult(newResultCode, cfg, context, params)
      } else if (prefix.startsWith("redirect")) {
        val targetResource = substringAfter(newResultCode, ":")
        if (contains(targetResource, ':')) { return new ServletRedirectResult(targetResource) }
        val action = buildAction(targetResource, false)

        // add special param and ajax tag for redirect
        val request: HttpServletRequest = ServletActionContext.getRequest()
        val redirectParamStrs = request.getParameterValues("params")
        if (null != redirectParamStrs) {
          for (redirectParamStr <- redirectParamStrs)
            action.params(redirectParamStr)
        }

        // x-requested-with->XMLHttpRequest
        if (null != request.getHeader("x-requested-with")) action.param("x-requested-with", "1")

        val params = buildResultParams(path, cfg)
        if (None != action.parameters.get("method")) {
          params.put("method", action.parameters("method"))
          action.parameters.remove("method")
        }

        if (isNotEmpty(action.method)) params.put("method", action.method)
        addNamespaceAction(action, params)

        val result = buildResult(newResultCode, cfg, context, params).asInstanceOf[ServletRedirectResult]
        for ((property, value) <- action.parameters) {
          result.addParameter(property, value)
        }
        result
      } else {
        // 从结果中抽取路径和返回值
        path = substringAfter(newResultCode, ":")
        newResultCode = "success"
        buildResult(newResultCode, cfg, context, buildResultParams(path, cfg))
      }
    }
  }

  /**
   * 依据跳转路径进行构建
   */
  private def buildAction(path: String, forward: Boolean): ToStruts = {
    var mapping: RequestMapping = null
    val action = ContextHolder.context.temp[Object]("dispatch_action") match {
      case action: To => {
        action match {
          case ca: ToClass => resolver.antiResolve(ca.clazz, ca.method) match {
            case Some(rm) =>
              if (forward) {
                mapping = rm
                new ToStruts(rm.action.namespace, rm.action.name, rm.action.method).params(ca.parameters)
              } else {
                rm.action.toURI(ca, ContextHolder.context.params).toStruts
              }
            case None => throw new RuntimeException(s"Cannot find action mapping for ${ca.clazz.getName} ${ca.method}")
          }
          case ua: ToURI => ua.toStruts
          case sa: ToStruts => sa
        }
      }
      case _ => {
        to(if (path.startsWith("?")) getServletPath(ServletActionContext.getRequest()) + path else path).toStruts
      }
    }
    if (forward) {
      if (null == mapping) {
        resolver.resolve(action.uri) match {
          case Some(m) => mapping = m
          case None => throw new RuntimeException(s"Cannot find action mapping for ${action.uri}")
        }
      }
      ActionContextHelper.setMapping(ContextHolder.context, mapping)
    }
    action
  }

  private def addNamespaceAction(action: ToStruts, params: collection.mutable.Map[String, String]) {
    params.put("namespace", action.namespace)
    params.put("actionName", action.name)
  }

  protected def buildResultParams(defaultParam: String, resultTypeConfig: ResultTypeConfig): collection.mutable.Map[String, String] = {
    import scala.collection.JavaConversions.mapAsScalaMap
    val params = new collection.mutable.LinkedHashMap[String, String]()
    if (resultTypeConfig.getParams() != null) {
      params ++= resultTypeConfig.getParams
    }
    params.put(resultTypeConfig.getDefaultResultParam(), defaultParam)
    params
  }

  /**
   * 构建结果
   */
  protected def buildResult(resultCode: String, cfg: ResultTypeConfig, context: ActionContext, params: collection.Map[String, String]): Result = {
    import scala.collection.JavaConversions.mapAsJavaMap
    val resultConfig = new ResultConfig.Builder(resultCode, cfg.getClassName()).addParams(params).build()
    try {
      objectFactory.buildResult(resultConfig, context.getContextMap())
    } catch {
      case e: Exception => throw new XWorkException("Unable to build convention result", e, resultConfig)
    }
  }

  @throws(classOf[XWorkException])
  def handleUnknownAction(namespace: String, actionName: String): ActionConfig = null

  @throws(classOf[NoSuchMethodException])
  def handleUnknownActionMethod(arg0: Object, arg1: String): Object = null
}