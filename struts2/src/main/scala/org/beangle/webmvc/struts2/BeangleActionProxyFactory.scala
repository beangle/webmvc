package org.beangle.webmvc.struts2

import java.{ util => ju }
import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.webmvc.api.context.ContextHolder
import com.opensymphony.xwork2.{ ActionProxy, DefaultActionInvocation, DefaultActionProxyFactory }
import com.opensymphony.xwork2.config.entities.ActionConfig
import com.opensymphony.xwork2.inject.Inject
import org.apache.struts2.ServletActionContext
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.view.DefaultViewMapper
import org.beangle.webmvc.dispatch.MethodHandler

class BeangleActionProxyFactory extends DefaultActionProxyFactory {

  override def createActionProxy(namespace: String, actionName: String, methodName: String, extraContext: ju.Map[String, Object], executeResult: Boolean, cleanupContext: Boolean): ActionProxy = {
    val inv = new BeangleActionInvocation(extraContext)
    container.inject(inv)
    createActionProxy(inv, namespace, actionName, methodName, executeResult, cleanupContext)
  }
}

class BeangleActionInvocation(extraContext: ju.Map[String, Object]) extends DefaultActionInvocation(extraContext, true) {

  protected override def invokeAction(action: Object, actionConfig: ActionConfig): String = {
    val mapping = ActionContextHelper.getMapping(ContextHolder.context)
    val result = String.valueOf(mapping.handler.handle(mapping.action))
    val resultCode = if (isEmpty(result)) "index"

    val viewName = DefaultViewMapper.defaultView(mapping.handler.asInstanceOf[MethodHandler].method.getName, result)
    saveResult(actionConfig, viewName)
  }
}