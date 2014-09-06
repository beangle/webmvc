package org.beangle.webmvc.execution.impl

import org.beangle.commons.bean.Initializing
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.api.context.{ ActionContext, ContextHolder }
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ ActionMapping, Configurer }
import org.beangle.webmvc.execution.{ Handler, Interceptor, InvocationReactor }
import org.beangle.webmvc.view.{ ViewRender, ViewResolver }
import org.beangle.webmvc.view.impl.DefaultTemplatePathMapper

@description("缺省的调用反应堆")
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
    val context = ContextHolder.context
    var lastInterceptorIndex = preHandle(interceptors, context, handler)
    var result: Any = null
    var exception: Throwable = null
    if (lastInterceptorIndex == interceptors.length - 1) {
      try {
        result = handler.handle(mapping)
      } catch {
        case ex: Throwable => exception = ex
      }
      //FIXME process exception
      if (null != exception) {
        postHandle(interceptors, context, handler, lastInterceptorIndex, result)
        throw exception
      } else {
        if (null != result) {
          try {
            val view = result match {
              case viewName: String =>
                val newViewName = DefaultTemplatePathMapper.defaultView(mapping.method.getName, viewName)
                config.views.get(newViewName) match {
                  case Some(v) => v
                  case None =>
                    val profile = configurer.getProfile(config.clazz.getName)
                    val newView = resolvers(profile.viewType).resolve(newViewName, mapping)
                    if (null == newView) throw new RuntimeException(s"Cannot find view[$newViewName] for ${config.clazz.getName}")
                    else newView
                }
              case view: View => view
            }
            if (null != view) {
              renders.get(view.getClass) match {
                case Some(render) => render.render(view, context)
                case None => throw new RuntimeException(s"Cannot find render for ${view.getClass}")
              }
            }
          } finally {
            postHandle(interceptors, context, handler, lastInterceptorIndex, result)
          }
        }
      }
    } else {
      postHandle(interceptors, context, handler, lastInterceptorIndex, result)
    }
  }

  def preHandle(interceptors: Array[Interceptor], context: ActionContext, handler: Handler): Int = {
    var i = 0
    while (i < interceptors.length) {
      val interceptor = interceptors(i)
      if (!interceptor.preHandle(context, handler)) return i
      i += 1
    }
    i - 1
  }

  def postHandle(interceptors: Array[Interceptor], context: ActionContext, handler: Handler, lastInterceptorIndex: Int, view: Any): Unit = {
    var i = lastInterceptorIndex
    while (i >= 0) {
      val interceptor = interceptors(i)
      interceptor.postHandle(context, handler, view)
      i -= 1
    }
  }
}