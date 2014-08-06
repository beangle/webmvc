package org.beangle.webmvc.struts2.factory

import java.{ util => ju }

import org.apache.struts2.views.freemarker.{ FreemarkerManager, FreemarkerResult }

import com.opensymphony.xwork2.{ ObjectFactory, Result }
import com.opensymphony.xwork2.config.entities.ResultConfig
import com.opensymphony.xwork2.factory.ResultFactory
import com.opensymphony.xwork2.inject.Inject
import com.opensymphony.xwork2.util.reflection.{ ReflectionException, ReflectionExceptionHandler, ReflectionProvider }

/**
 * 为freemaker做优化
 */
class BeangleResultFactory extends ResultFactory {

  protected var freemarkerManager: FreemarkerManager = _
  protected var objectFactory: ObjectFactory = _
  protected var reflectionProvider: ReflectionProvider = _

  @Inject
  def this(objectFactory: ObjectFactory, freemarkerManager: FreemarkerManager, reflectionProvider: ReflectionProvider) {
    this()
    this.objectFactory = objectFactory
    this.freemarkerManager = freemarkerManager
    this.reflectionProvider = reflectionProvider
  }

  def buildResult(resultConfig: ResultConfig, extraContext: ju.Map[String, Object]): Result = {
    val resultClassName: String = resultConfig.getClassName()
    if (resultClassName != null) {
      if (resultClassName.equals("org.apache.struts2.views.freemarker.FreemarkerResult")) {
        val result = new FreemarkerResult(resultConfig.getParams().get("location"))
        result.setFreemarkerManager(freemarkerManager)
        result
      } else {
        val result = objectFactory.buildBean(resultClassName, extraContext).asInstanceOf[Result]
        val params = resultConfig.getParams()
        if (params != null) {
          import scala.collection.JavaConversions.asScalaSet
          params.entrySet().foreach { paramEntry =>
            try {
              reflectionProvider.setProperty(paramEntry.getKey(), paramEntry.getValue(), result, extraContext, true)
            } catch {
              case ex: ReflectionException => {
                if (result.isInstanceOf[ReflectionExceptionHandler])
                  result.asInstanceOf[ReflectionExceptionHandler].handle(ex)
              }
            }
          }
        }
        result
      }
    } else null
  }
}