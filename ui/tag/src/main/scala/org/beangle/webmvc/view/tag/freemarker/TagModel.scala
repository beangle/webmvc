package org.beangle.webmvc.view.tag.freemarker

import java.io.Writer
import java.lang.reflect.Constructor
import java.{ util => ju }

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.view.tag.{ Component, ComponentContext }

import freemarker.ext.beans.BeansWrapper
import freemarker.template.{ TemplateModel, TemplateModelException, TemplateTransformModel }

class TagModel(context: ComponentContext, clazz: Class[_ <: Component] = null) extends TemplateTransformModel with Logging {

  private val componentCon: Constructor[_ <: Component] = if (clazz != null) clazz.getConstructor(classOf[ComponentContext]) else null

  def getWriter(writer: Writer, params: ju.Map[_, _]): Writer = {
    val bean = getBean()
    val objectWrapper = BeansWrapper.getDefaultInstance()
    val iterator = params.keySet().iterator()
    while (iterator.hasNext()) {
      var key = iterator.next().asInstanceOf[String]
      var value = params.get(key).asInstanceOf[Object]
      if (value != null) {
        if (PropertyUtils.isWriteable(bean, key.asInstanceOf[String])) {
          if (value.isInstanceOf[TemplateModel]) {
            try {
              value = objectWrapper.unwrap(value.asInstanceOf[TemplateModel])
            } catch {
              case e: TemplateModelException =>
                error("failed to unwrap [" + value + "] it will be ignored", e)
            }
          }
          try {
            PropertyUtils.setProperty(bean, key, value)
          } catch {
            case e: Exception =>
              error("invoke set property [" + key + "] with value " + value, e)
          }
        } else {
          bean.parameters.put(key, value)
        }
      }
    }
    return new ResetCallbackWriter(bean, writer)
  }

  protected def getBean(): Component = {
    try {
      return componentCon.newInstance(context)
    } catch {
      case e: Exception =>
        throw new RuntimeException(e)
    }
  }
}