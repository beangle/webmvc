package org.beangle.webmvc.struts2

import java.{ util => ju }
import com.opensymphony.xwork2.{ ActionProxy, DefaultActionInvocation, DefaultActionProxyFactory }
import com.opensymphony.xwork2.config.entities.ActionConfig
import org.beangle.commons.lang.reflect.ClassInfo
import java.lang.reflect.Constructor
import org.beangle.webmvc.helper.Params
import org.beangle.webmvc.annotation.param
import org.beangle.commons.lang.Primitives

class BeangleActionProxyFactory extends DefaultActionProxyFactory {

  override def createActionProxy(namespace: String, actionName: String, methodName: String, extraContext: ju.Map[String, Object], executeResult: Boolean, cleanupContext: Boolean): ActionProxy = {
    val inv = new BeangleActionInvocation(extraContext)
    container.inject(inv)
    createActionProxy(inv, namespace, actionName, methodName, executeResult, cleanupContext)
  }
}

class BeangleActionInvocation(extraContext: ju.Map[String, Object]) extends DefaultActionInvocation(extraContext, true) {

  protected override def invokeAction(action: Object, actionConfig: ActionConfig): String = {
    val methodName = proxy.getMethod
    val methods = ClassInfo.get(action.getClass).getMethods(methodName)
    var methodResult: AnyRef = null
    if (methods.size != 1) {
      if (unknownHandlerManager.hasUnknownHandlers()) {
        methodResult = unknownHandlerManager.handleUnknownMethod(action, methodName)
      } else {
        throw new IllegalArgumentException(s"The $methodName is not unique defined in action ${getAction.getClass}")
      }
    } else {
      val methodinfo = methods.head
      val method = methodinfo.method
      val paramLength = methodinfo.parameterTypes.length
      if (0 == paramLength) {
        methodResult = method.invoke(action)
      } else {
        val annotationsList = method.getParameterAnnotations()
        val values = new collection.mutable.ListBuffer[Object]
        var i = 0;
        for (annotations <- annotationsList) {
          for (annotation <- annotations) {
            annotation match {
              case p: param => {
                val pt = methodinfo.parameterTypes(i)
                if (Primitives.isWrapperType(pt)) {
                  values += (Params.get(p.value, pt).getOrElse(null)).asInstanceOf[Object]
                } else {
                  val value = (Params.get(p.value, Primitives.wrap(pt)).getOrElse(null)).asInstanceOf[Object]
                  if (null == value) {
                    //FIXME binding error , migrate from other framework process this situation.
                  } else {
                    values += value
                  }
                }
              }
              case _ =>
            }
          }
          i += 1
        }
        if (values.size != paramLength) {
          throw new IllegalArgumentException(s"Cannot  bind parameter to $methodName in action ${getAction.getClass}")
        } else {
          methodResult = method.invoke(action, values.toArray: _*)
        }
      }
    }
    saveResult(actionConfig, methodResult)
  }
}