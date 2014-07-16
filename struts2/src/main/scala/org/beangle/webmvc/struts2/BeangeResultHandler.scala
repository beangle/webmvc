package org.beangle.webmvc.struts2

import org.beangle.webmvc.struts2.factory.ResultBuilder

import com.opensymphony.xwork2.{ActionContext, Result, UnknownHandler, XWorkException}
import com.opensymphony.xwork2.config.entities.ActionConfig
import com.opensymphony.xwork2.inject.Inject

/**
 * 实现action到result之间的路由和处理<br>
 * 默认按照方法进行路由
 *
 * @author chaostone
 */
class BeangleResultHandler extends UnknownHandler {

  @Inject
  protected var resultBuilder: ResultBuilder = _

  @throws(classOf[XWorkException])
  def handleUnknownAction(namespace: String, actionName: String): ActionConfig = null

  @throws(classOf[NoSuchMethodException])
  def handleUnknownActionMethod(arg0: Object, arg1: String): Object = null
  
  @throws(classOf[XWorkException])
  def handleUnknownResult(actionContext: ActionContext, actionName: String,
    actionConfig: ActionConfig, resultCode: String): Result = resultBuilder.build(resultCode, actionConfig, actionContext)

}