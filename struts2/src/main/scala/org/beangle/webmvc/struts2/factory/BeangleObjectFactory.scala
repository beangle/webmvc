package org.beangle.webmvc.struts2.factory

import java.{ util => ju }

import org.beangle.commons.inject.{ Container, ContainerAware }
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.helper.ContainerHelper

import com.opensymphony.xwork2.ObjectFactory
import com.opensymphony.xwork2.factory.ResultFactory
import com.opensymphony.xwork2.inject.Inject

@SerialVersionUID(-1733081389212973935L)
class BeangleObjectFactory extends ObjectFactory with ContainerAware with Logging {

  var container: Container = ContainerHelper.get
  /**
   * Looks up beans using application context before falling back to the method defined
   * in the {@link ObjectFactory}.
   */
  @throws(classOf[Exception])
  override def buildBean(beanName: String, extraContext: ju.Map[String, Object], injectInternal: Boolean): Object = {
    val opBean: Option[Object] = container.getBean(beanName)
    opBean match {
      case Some(bean) => bean
      case None => buildBean(getClassInstance(beanName), extraContext)
    }
  }

  @throws(classOf[Exception])
  override def buildBean(clazz: Class[_], extraContext: ju.Map[String, Object]): Object = {
    val bean = clazz.newInstance()
    injectInternalBeans(bean)
  }

  @throws(classOf[ClassNotFoundException])
  override def getClassInstance(className: String): Class[_] = {
    var clazz: Class[_] = null
    if (container.contains(className)) clazz = container.getType(className).get
    else clazz = super.getClassInstance(className)
    clazz
  }

  @Inject("beangle")
  override def setResultFactory(resultFactory: ResultFactory): Unit = {
    super.setResultFactory(resultFactory)
  }
}