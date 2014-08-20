package org.beangle.webmvc.execution.impl

import org.beangle.webmvc.execution.InvocationReactor
import org.beangle.webmvc.dispatch.ActionMapping
import org.beangle.webmvc.execution.Handler
import org.beangle.webmvc.execution.Interceptor
import org.beangle.commons.inject.Container
import org.beangle.commons.bean.Initializing
import org.beangle.webmvc.view.ViewResolver
import org.beangle.webmvc.view.ViewRender
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.view.impl.DefaultViewMapper

class DefaultInvocationReactor extends InvocationReactor with Initializing {

  var container: Container = _

  var resolvers: List[ViewResolver] = List.empty

  var renders: Map[Class[_], ViewRender] = Map.empty

  override def init(): Unit = {
    resolvers = container.getBeans(classOf[ViewResolver]).values.toList
    val renderMaps = new collection.mutable.HashMap[Class[_], ViewRender]
    container.getBeans(classOf[ViewRender]).values foreach { render =>
      renderMaps.put(render.supportView, render)
    }
    renders = renderMaps.toMap
  }

  override def invoke(handler: Handler, mapping: ActionMapping): Unit = {
    val interceptors = mapping.interceptors
    var lastInterceptorIndex = preHandle(interceptors, handler)
    var result: Any = null
    if (lastInterceptorIndex == interceptors.length - 1) {
      result = handler.handle(mapping)
      if (null != result) {
        val view = result match {
          case viewName: String => mapping.views.get(viewName) match {
            case Some(v) => v
            case None =>
              val newViewName = DefaultViewMapper.defaultView(mapping.method, viewName)
              var resolvedView: View = null
              val iter = resolvers.iterator
              while (iter.hasNext && null == resolvedView) {
                val resolver = iter.next
                resolvedView = resolver.resolve(newViewName, mapping)
              }
              resolvedView
          }
          case view: View => view
        }
        if (null == view) {
          throw new RuntimeException(s"Cannot find $result for ${mapping.clazz}")
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