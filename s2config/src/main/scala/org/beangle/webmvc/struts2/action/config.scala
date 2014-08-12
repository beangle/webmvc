/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.struts2.action

import scala.collection.JavaConversions.asScalaSet

import org.apache.struts2.StrutsConstants.{ STRUTS_ACTIONPROXYFACTORY, STRUTS_ACTION_EXTENSION, STRUTS_FREEMARKER_MANAGER_CLASSNAME, STRUTS_MAPPER_CLASS, STRUTS_MULTIPART_PARSER, STRUTS_OBJECTFACTORY, STRUTS_OBJECTFACTORY_ACTIONFACTORY, STRUTS_OBJECTFACTORY_RESULTFACTORY, STRUTS_OBJECTTYPEDETERMINER, STRUTS_URL_RENDERER, STRUTS_VELOCITY_MANAGER_CLASSNAME, STRUTS_XWORKCONVERTER, STRUTS_XWORKTEXTPROVIDER }
import org.apache.struts2.components.UrlRenderer
import org.apache.struts2.dispatcher.mapper.ActionMapper
import org.apache.struts2.dispatcher.multipart.MultiPartRequest
import org.apache.struts2.views.freemarker.FreemarkerManager
import org.apache.struts2.views.velocity.VelocityManager
import org.beangle.commons.lang.{ ClassLoaders, Objects }
import org.beangle.commons.lang.reflect.BeanManifest
import org.beangle.webmvc.action.ActionSupport
import org.beangle.webmvc.route.Action
import org.beangle.webmvc.struts2.action.helper.S2ConfigurationHelper

import com.opensymphony.xwork2.{ ActionContext, ActionProxyFactory, ObjectFactory, TextProvider }
import com.opensymphony.xwork2.conversion.ObjectTypeDeterminer
import com.opensymphony.xwork2.conversion.impl.XWorkConverter
import com.opensymphony.xwork2.factory.{ ActionFactory, ResultFactory }
import com.opensymphony.xwork2.inject.Container
/**
 * @author chaostone
 */
class ConfigAction extends ActionSupport {

  import ConfigAction._

  private def getConfigHelper(): S2ConfigurationHelper = {
    return ActionContext.getContext().getContainer().getInstance(classOf[S2ConfigurationHelper])
  }

  def index(): String = {
    return forward(Action(this, "actions"))
  }

  def actions(): String = {
    val configHelper = getConfigHelper()
    get("namespace") match {
      case Some(namespace) =>
        put("namespace", namespace)
        put("actionNames", configHelper.getActionNames(namespace).toList.sorted)
      case None =>
        val namespaces = configHelper.getNamespaces()
        if (namespaces.size == 0) {
          addError("There are no namespaces in this configuration")
          return "error"
        }
        put("namespaces", namespaces)
    }
    forward()
  }

  def action(): String = {
    val configHelper = getConfigHelper()
    val namespace = get("namespace", "")
    val actionName = get("actionName", "")
    val config = configHelper.getActionConfig(namespace, actionName)
    put("actionNames", configHelper.getActionNames(namespace))
    try {
      val clazz = configHelper.objectFactory.getClassInstance(config.getClassName())
      put("properties", BeanManifest.get(clazz).getters.values.filterNot(m => m.method.getName.contains("$")))
    } catch {
      case e: Throwable =>
        error("Unable to get properties for action " + actionName, e)
        addError("Unable to retrieve action properties: " + e.toString())
    }
    var extension: String = configHelper.container.getInstance(classOf[String], STRUTS_ACTION_EXTENSION)
    if (extension != null && extension.indexOf(",") > -1) extension = extension.substring(0, extension.indexOf(","))
    put("detailView", get("detailView", "results"))
    put("extension", extension)
    put("config", config)
    //FIMXE cannot write ${config.result.values()} why
    put("results", config.getResults.values())
    put("namespace", namespace)
    put("actionName", actionName)
    forward()
  }

  def beans(): String = {
    val configHelper = getConfigHelper()
    val container = configHelper.container
    val bindings = new collection.mutable.HashSet[Binding]
    addBinding(bindings, container, classOf[ObjectFactory], STRUTS_OBJECTFACTORY)
    addBinding(bindings, container, classOf[XWorkConverter], STRUTS_XWORKCONVERTER)
    addBinding(bindings, container, classOf[TextProvider], STRUTS_XWORKTEXTPROVIDER)
    addBinding(bindings, container, classOf[ActionProxyFactory], STRUTS_ACTIONPROXYFACTORY)
    addBinding(bindings, container, classOf[ObjectTypeDeterminer], STRUTS_OBJECTTYPEDETERMINER)
    addBinding(bindings, container, classOf[ActionMapper], STRUTS_MAPPER_CLASS)
    addBinding(bindings, container, classOf[MultiPartRequest], STRUTS_MULTIPART_PARSER)
    addBinding(bindings, container, classOf[FreemarkerManager], STRUTS_FREEMARKER_MANAGER_CLASSNAME)
    addBinding(bindings, container, classOf[VelocityManager], STRUTS_VELOCITY_MANAGER_CLASSNAME)
    addBinding(bindings, container, classOf[UrlRenderer], STRUTS_URL_RENDERER)
    addBinding(bindings, container, classOf[ActionFactory], STRUTS_OBJECTFACTORY_ACTIONFACTORY)
    addBinding(bindings, container, classOf[ResultFactory], STRUTS_OBJECTFACTORY_RESULTFACTORY)
    put("beans", bindings)
    forward()
  }

  private def addBinding(bindings: collection.mutable.Set[Binding], container: Container, typ: Class[_], constName: String): Unit = {
    var chosenName = container.getInstance(classOf[String], constName)
    if (chosenName == null) chosenName = "struts"
    val names = container.getInstanceNames(typ)
    if (null != names) {
      if (!names.contains(chosenName)) {
        bindings.add(new Binding(typ.getName(), getInstanceClassName(container, typ, "default"), chosenName, constName, true))
      }
      for (name <- names) {
        if (!"default".equals(name)) bindings.add(new Binding(typ.getName, getInstanceClassName(container, typ, name), name, constName, name.equals(chosenName)))
      }
    }
  }

  private def getInstanceClassName(container: Container, typ: Class[_], name: String): String = {
    var instName = "Class unable to be loaded"
    try {
      val inst = container.getInstance(typ, name)
      if (null != inst) instName = inst.getClass().getName()
    } catch {
      case e: Throwable =>
    }
    instName
  }

  def consts(): String = {
    val configHelper = getConfigHelper()
    val consts = new collection.mutable.HashMap[String, String]
    for (key <- configHelper.container.getInstanceNames(classOf[String])) {
      consts.put(key, configHelper.container.getInstance(classOf[String], key))
    }
    put("consts", consts)
    forward()
  }

  def jars(): String = {
    val configHelper = getConfigHelper()
    put("jarPoms", configHelper.getJarProperties())
    put("pluginsLoaded", ClassLoaders.getResources("struts-plugin.xml"))
    forward()
  }

  private def getClassInstance(clazz: String): Class[_] = {
    try {
      return ClassLoaders.loadClass(clazz)
    } catch {
      case e: Throwable =>
    }
    null
  }
  def stripPackage(clazz: String): String = {
    clazz.substring(clazz.lastIndexOf('.') + 1)
  }

  def stripPackage(clazz: Class[_]): String = {
    clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1)
  }

}

object ConfigAction {
  class Binding(val typ: String, val impl: String, val alias: String, val constant: String, val isDefault: Boolean) extends Ordered[Binding] {

    override def compare(b2: Binding): Int = {
      if (isDefault) -1
      else if (b2.isDefault) 1
      else alias.compareTo(b2.alias)
    }
  }
  class PropertyInfo(val name: String, val typ: Class[_], val value: Object) extends Ordered[PropertyInfo] {

    override def equals(o: Any): Boolean = {
      if (this == o) return true
      o match {
        case pi: PropertyInfo =>
          if (!name.equals(pi.name)) return false
          if (!typ.equals(pi.typ)) return false
          Objects.equals(value, pi.value)
        case _ => false
      }
    }

    override def hashCode(): Int = {
      var result = name.hashCode()
      result = 29 * result + typ.hashCode()
      result = 29 * result + (if (value != null) value.hashCode() else 0)
      result
    }

    def compare(other: PropertyInfo): Int = {
      return this.name.compareTo(other.name)
    }
  }
}
