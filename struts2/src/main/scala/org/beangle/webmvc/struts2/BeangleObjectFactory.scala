package org.beangle.webmvc.struts2

import java.{util => ju}

import org.beangle.commons.inject.{Container, ContainerAware, Containers}
import org.beangle.commons.logging.Logging

import com.opensymphony.xwork2.ObjectFactory
import com.opensymphony.xwork2.factory.ResultFactory
import com.opensymphony.xwork2.inject.Inject
import org.apache.struts2.StrutsConstants
import javax.servlet.ServletContext

@SerialVersionUID(-1733081389212973935L)
class BeangleObjectFactory extends ObjectFactory with ContainerAware with Logging {

  var container: Container = _

  /**
   * Constructs the object factory
   * @since 2.1.3
   */
  @Inject
  def this(@Inject servletContext: ServletContext, @Inject(StrutsConstants.STRUTS_DEVMODE) devMode: String) {
    this()
    container = Containers.root
    if (container == null) error("Cannot find beangle context from ServletContext")
  }

  /**
   * Looks up beans using application context before falling back to the method defined
   * in the {@link ObjectFactory}.
   */
  @throws(classOf[Exception])
  override def buildBean(beanName: String, extraContext: ju.Map[String, Object], injectInternal: Boolean): Object = {
    if (container.contains(beanName)) {
      val bean: Object = container.getBean(beanName).get
      if (injectInternal) injectInternalBeans(bean)
      bean
    } else {
      buildBean(getClassInstance(beanName), extraContext)
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