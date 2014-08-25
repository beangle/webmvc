package org.beangle.webmvc.execution.impl

import org.beangle.commons.bean.Initializing
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ ActionMapping, Configurer }
import org.beangle.webmvc.execution.{ Handler, Interceptor, InvocationReactor }
import org.beangle.webmvc.view.{ ViewRender, ViewResolver }
import org.beangle.webmvc.view.template.DefaultTemplatePathMapper

@description("缺省的调用发生器")
class DefaultInvocationReactor extends InvocationReactor with Initializing {

  var container: Container = _

  var resolvers: Map[String, ViewResolver] = Map.empty

  var renders: Map[Class[_], ViewRender] = Map.empty

  var configurer: Configurer = _

  override def init(): Unit = {
    val resolverMap = new collection.mutable.HashMap[String, ViewResolver]
    container.getBeans(classOf[ViewResolver]).values foreach { resolver =>
      resolverMap.put(resolver.supportViewType, resolver)
    }
    resolvers = resolverMap.toMap

    val renderMaps = new collection.mutable.HashMap[Class[_], ViewRender]
    container.getBeans(classOf[ViewRender]).values foreach { render =>
      renderMaps.put(render.supportViewClass, render)
    }
    renders = renderMaps.toMap
  }

  override def invoke(handler: Handler, mapping: ActionMapping): Unit = {
    val config = mapping.config
    val interceptors = config.profile.interceptors
    var lastInterceptorIndex = preHandle(interceptors, handler)
    var result: Any = null
    if (lastInterceptorIndex == interceptors.length - 1) {
      result = handler.handle(mapping)
      if (null != result) {
        val view = result match {
          case viewName: String =>
            val newViewName = DefaultTemplatePathMapper.defaultView(mapping.method.getName, viewName)
            config.views.get(newViewName) match {
              case Some(v) => v
              case None =>
                val profile = configurer.getProfile(config.clazz.getName)
                resolvers(profile.viewType).resolve(newViewName, mapping)
            }
          case view: View => view
        }
        if (null == view) {
          throw new RuntimeException(s"Cannot find $result for ${config.clazz}")
        }
        renders.get(view.getClass) match {
          case Some(render) => render.render(view, ContextHolder.context)
          case None => throw new RuntimeException(s"Cannot find render for ${view.getClass}")
        }
      }
    }
    postHandle(interceptors, handler, lastInterceptorIndex, result)
  }

  def preHandle(interceptors: Array[Interceptor], handler: Handler): Int = {
    var i = 0
    while (i < interceptors.length) {
      val interceptor = interceptors(i)
      if (!interceptor.preHandle(handler)) return i
      i += 1
    }
    i - 1
  }

  def postHandle(interceptors: Array[Interceptor], handler: Handler, lastInterceptorIndex: Int, view: Any): Unit = {
    var i = lastInterceptorIndex
    while (i >= 0) {
      val interceptor = interceptors(i)
      interceptor.postHandle(handler, view)
      i -= 1
    }
  }
}