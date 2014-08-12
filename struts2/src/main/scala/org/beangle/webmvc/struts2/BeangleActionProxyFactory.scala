package org.beangle.webmvc.struts2

import java.{ util => ju }
import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.route.impl.{ DefaultViewMapper, MethodHandler }
import com.opensymphony.xwork2.{ ActionProxy, DefaultActionInvocation, DefaultActionProxyFactory }
import com.opensymphony.xwork2.config.entities.ActionConfig
import org.beangle.webmvc.route.RequestMapper
import com.opensymphony.xwork2.inject.Inject
import org.apache.struts2.ServletActionContext

class BeangleActionProxyFactory extends DefaultActionProxyFactory {

  override def createActionProxy(namespace: String, actionName: String, methodName: String, extraContext: ju.Map[String, Object], executeResult: Boolean, cleanupContext: Boolean): ActionProxy = {
    val inv = new BeangleActionInvocation(extraContext)
    container.inject(inv)
    createActionProxy(inv, namespace, actionName, methodName, executeResult, cleanupContext)
  }
}

class BeangleActionInvocation(extraContext: ju.Map[String, Object]) extends DefaultActionInvocation(extraContext, true) {

  protected override def invokeAction(action: Object, actionConfig: ActionConfig): String = {
    val mapping = ContextHolder.context.mapping
    val result = String.valueOf(mapping.handler.handle(mapping.action))
    val resultCode = if (isEmpty(result)) "index"

    val viewName = DefaultViewMapper.defaultView(mapping.handler.asInstanceOf[MethodHandler].method.getName, result)
    saveResult(actionConfig, viewName)
  }
}