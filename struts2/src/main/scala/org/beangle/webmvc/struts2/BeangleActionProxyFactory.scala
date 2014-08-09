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

  @Inject
  var resolver: RequestMapper = _

  override def createActionProxy(namespace: String, actionName: String, methodName: String, extraContext: ju.Map[String, Object], executeResult: Boolean, cleanupContext: Boolean): ActionProxy = {
    //when chain result invocated servletActionContext.actionMapping is not null
    if (null != ServletActionContext.getActionMapping()) {
      resolver.resolve(namespace + "/" + actionName + "/" + methodName) match {
        case Some(m) => ContextHolder.context.actionMapping = m
        case None => throw new RuntimeException("Cannot find action mapping for $namespace $actionName $methodName")
      }
    }
    val inv = new BeangleActionInvocation(extraContext)
    container.inject(inv)
    createActionProxy(inv, namespace, actionName, methodName, executeResult, cleanupContext)
  }
}

class BeangleActionInvocation(extraContext: ju.Map[String, Object]) extends DefaultActionInvocation(extraContext, true) {

  protected override def invokeAction(action: Object, actionConfig: ActionConfig): String = {
    val mapping = ContextHolder.context.actionMapping
    val result = String.valueOf(mapping.handler.handle(mapping.params))
    val resultCode = if (isEmpty(result)) "index"

    val viewName = DefaultViewMapper.defaultView(mapping.handler.asInstanceOf[MethodHandler].method.getName, result)
    saveResult(actionConfig, viewName)
  }
}