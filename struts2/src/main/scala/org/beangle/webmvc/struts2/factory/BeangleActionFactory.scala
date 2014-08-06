package org.beangle.webmvc.struts2.factory

import java.{ util => ju }

import org.beangle.commons.inject.{ Container, ContainerAware }
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.helper.ContainerHelper

import com.opensymphony.xwork2.config.entities.ActionConfig
import com.opensymphony.xwork2.factory.ActionFactory

@SerialVersionUID(-1733081389212973935L)
class BeangleActionFactory extends ActionFactory with ContainerAware with Logging {

  var container: Container = ContainerHelper.get

  @throws(classOf[Exception])
  override def buildAction(beanName: String, namespace: String, config: ActionConfig, extraContext: ju.Map[String, Object]): Object = {
    container.getBean(config.getClassName).orNull
  }
}