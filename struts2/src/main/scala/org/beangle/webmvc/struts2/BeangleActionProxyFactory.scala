package org.beangle.webmvc.struts2

import java.{ util => ju }
import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.execution.impl.MethodHandler
import org.beangle.webmvc.view.impl.DefaultViewMapper
import com.opensymphony.xwork2.{ ActionProxy, DefaultActionInvocation, DefaultActionProxyFactory }
import com.opensymphony.xwork2.config.entities.ActionConfig
import com.opensymphony.xwork2.ActionInvocation
import com.opensymphony.xwork2.ActionEventListener
import com.opensymphony.xwork2.interceptor.PreResultListener
import com.opensymphony.xwork2.ActionContext
import org.beangle.webmvc.execution.InvocationReactor
import org.beangle.webmvc.helper.ContainerHelper

class BeangleActionProxyFactory extends DefaultActionProxyFactory {

  val reactor = ContainerHelper.get.getBean(classOf[InvocationReactor]).get

  override def createActionProxy(namespace: String, actionName: String, methodName: String, extraContext: ju.Map[String, Object], executeResult: Boolean, cleanupContext: Boolean): ActionProxy = {
    new BeangleActionProxy(reactor, null, namespace, actionName, methodName)
  }
}

class BeangleActionProxy(reactor: InvocationReactor, val action: Object, val namespace: String, val name: String, val methodName: String) extends ActionProxy {
  override def getAction(): Object = action

  override def getActionName: String = name

  override def getConfig(): ActionConfig = null

  override def setExecuteResult(executeResult: Boolean): Unit = {}

  override def getExecuteResult: Boolean = true

  override def getInvocation(): ActionInvocation = BeangleActionInvocation

  override def getNamespace(): String = namespace;

  override def execute(): String = {
    val mapping = ActionContextHelper.getMapping(ContextHolder.context)
    reactor.invoke(mapping.handler, mapping.action);
    null
  }

  override def getMethod(): String = methodName

  override def isMethodSpecified: Boolean = true
}

object BeangleActionInvocation extends ActionInvocation {

  override def getAction() = null

  override def isExecuted() = true

  override def getInvocationContext(): ActionContext = null;

  override def getProxy() = null

  override def getResult() = null

  override def getResultCode(): String = null

  override def setResultCode(resultCode: String): Unit = {}

  override def getStack() = null;

  override def addPreResultListener(listener: PreResultListener): Unit = {}

  override def invoke(): String = null

  override def invokeActionOnly(): String = null

  override def setActionEventListener(listener: ActionEventListener): Unit = {}

  override def init(proxy: ActionProxy): Unit = {}

  override def serialize: ActionInvocation = null

  override def deserialize(actionContext: ActionContext): ActionInvocation = null
}