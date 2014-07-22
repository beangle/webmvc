package org.beangle.webmvc.struts2.factory

import org.apache.struts2.ServletActionContext
import org.apache.struts2.dispatcher.ServletRedirectResult
import org.apache.struts2.views.freemarker.FreemarkerManager
import org.beangle.commons.lang.Strings.{ contains, isBlank, isEmpty, isNotEmpty, substringAfter, substringBefore }
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.RequestUtils.getServletPath
import com.opensymphony.xwork2.{ ActionContext, ObjectFactory, Result, XWorkException }
import com.opensymphony.xwork2.config.Configuration
import com.opensymphony.xwork2.config.entities.{ ActionConfig, ResultConfig, ResultTypeConfig }
import com.opensymphony.xwork2.inject.Inject
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.view.freemarker.TemplateFinderByConfig
import org.beangle.webmvc.route.ViewMapper
import org.beangle.webmvc.route.ActionBuilder
import org.beangle.webmvc.route.Action
import org.beangle.webmvc.route.RouteService

/**
 * 为构建自定义的结果，抽象出的一个接口
 *
 * @author chaostone
 */
trait ResultBuilder {

  def build(resultCode: String, actionConfig: ActionConfig, context: ActionContext): Result
}

class DefaultResultBuilder extends ResultBuilder with Logging {

  protected var resultTypeConfigs: Map[String, ResultTypeConfig] = Map.empty

  protected var objectFactory: ObjectFactory = _

  protected var configuration: Configuration = _

  protected var routeService: RouteService = _

  private var templateFinder: TemplateFinderByConfig = _

  @Inject
  def this(configuration: Configuration, objectFactory: ObjectFactory, freemarkerManager: FreemarkerManager, routeService: RouteService) = {
    this()
    this.objectFactory = objectFactory
    this.configuration = configuration
    this.routeService = routeService
    this.templateFinder = new TemplateFinderByConfig(freemarkerManager.getConfig(), routeService.viewMapper)
    val typeExtensions = Map(("freemarker", "ftl"), ("velocity", "vm"), ("dispatcher", "jsp"))
    val pc = configuration.getPackageConfig("struts-default")
    val resTypeConfigs = new collection.mutable.HashMap[String, ResultTypeConfig]
    import scala.collection.JavaConversions.asScalaSet
    for (name <- pc.getAllResultTypeConfigs().keySet()) {
      val rtc = pc.getAllResultTypeConfigs().get(name)
      typeExtensions.get(name).foreach { extension => resTypeConfigs.put(extension, rtc) }
      resTypeConfigs.put(name, rtc)
    }
    this.resultTypeConfigs = resTypeConfigs.toMap
  }

  def build(resultCode: String, actionConfig: ActionConfig, context: ActionContext): Result = {
    var path: String = null
    var cfg: ResultTypeConfig = null
    var newResultCode = resultCode
    // first route by common result
    if (!contains(newResultCode, ':')) {
      val className = context.getActionInvocation().getProxy().getAction().getClass().getName()
      val methodName = context.getActionInvocation().getProxy().getMethod()
      if (isEmpty(newResultCode)) newResultCode = "index"

      val buf = new StringBuilder()
      buf.append(routeService.mapView(className, methodName, newResultCode))
      buf.append('.')
      buf.append(routeService.getProfile(className).viewExtension)
      path = buf.toString()
      cfg = resultTypeConfigs("freemarker")
      return buildResult(newResultCode, cfg, context, buildResultParams(path, cfg))
    } else {
      // by prefix
      val prefix = substringBefore(newResultCode, ":")
      cfg = resultTypeConfigs(prefix)
      if (prefix.startsWith("chain")) {
        val action = buildAction(substringAfter(newResultCode, ":"))
        val params = buildResultParams(path, cfg)
        addNamespaceAction(action, params)
        if (isNotEmpty(action.method)) params.put("method", action.method)

        buildResult(newResultCode, cfg, context, params)
      } else if (prefix.startsWith("redirect")) {
        val targetResource = substringAfter(newResultCode, ":")
        if (contains(targetResource, ':')) { return new ServletRedirectResult(targetResource) }
        val action = buildAction(targetResource)

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
   *
   * @param path
   * @param param
   * @param redirectParamStr
   */
  private def buildAction(path: String): Action = {
    ActionContext.getContext().getContextMap().get("dispatch_action") match {
      case action: Action => {
        if (null != action.clazz) {
          val newAction = routeService.buildAction(action.clazz)
          action.name = newAction.name
          action.namespace = newAction.namespace
        }
        if (isBlank(action.name)) {
          action.path(getServletPath(ServletActionContext.getRequest()))
        }
        action
      }
      case _ => {
        val newPath = if (path.startsWith("?")) {
          getServletPath(ServletActionContext.getRequest()) + path
        } else path
        new Action(newPath, null)
      }
    }
  }

  private def addNamespaceAction(action: Action, params: collection.mutable.Map[String, String]) {
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
}