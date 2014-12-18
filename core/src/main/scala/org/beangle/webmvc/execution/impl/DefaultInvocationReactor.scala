package org.beangle.webmvc.execution.impl

import org.beangle.commons.bean.Initializing
import org.beangle.commons.http.accept.ContentNegotiationManager
import org.beangle.commons.inject.Container
import org.beangle.commons.io.Serializer
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.web.intercept.Interceptor
import org.beangle.webmvc.api.context.{ ActionContext, ContextHolder }
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ ActionMapping, Configurer }
import org.beangle.webmvc.context.SerializerManager
import org.beangle.webmvc.execution.{ Handler, InvocationReactor }
import org.beangle.webmvc.view.{ ViewRender, ViewResolver }
import javax.activation.MimeType
import org.beangle.webmvc.view.impl.ViewResolverRegistry
/**
 * 缺省的调用反应堆
 * 负责调用Action,渲染结果
 */
@description("缺省的调用反应堆")
class DefaultInvocationReactor extends InvocationReactor with Initializing {

  var container: Container = _

  var viewResolverRegistry: ViewResolverRegistry = _

  var renders: Map[Class[_], ViewRender] = Map.empty

  var serializerManager: SerializerManager = _

  var contentNegotiationManager: ContentNegotiationManager = _

  var configurer: Configurer = _

  override def init(): Unit = {
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
    var lastInterceptorIndex = preHandle(interceptors, context)
    var result: Any = null
    var exception: Throwable = null
    if (lastInterceptorIndex == interceptors.length - 1) {
      try {
        result = handler.handle(mapping)
      } catch {
        case ex: Throwable => exception = ex
      }
      if (null != exception) {
        //FIXME process exception
        postHandle(interceptors, context, lastInterceptorIndex)
        throw exception
      } else {
        if (null == result && null != mapping.defaultView) result = mapping.defaultView
        if (null != result) {
          try {
            val view =
              result match {
                case viewName: String =>
                  if (null == mapping.defaultView) null
                  else {
                    config.views.get(viewName) match {
                      case Some(v) => v
                      case None =>
                        val profile = configurer.getProfile(config.clazz.getName)
                        val candidates = Strings.split(viewName, ",")
                        var newView: View = null
                        var i = 0
                        val resolver = viewResolverRegistry.getResolver(profile.viewType).get
                        while (i < candidates.length && null == newView) {
                          newView = resolver.resolve(candidates(i), mapping)
                          i += 1
                        }
                        require(null != newView, s"Cannot find view[$viewName] for ${config.clazz.getName}")
                        newView
                    }
                  }
                case view: View => view
                case _ => null
              }
            if (null != view) {
              renders.get(view.getClass) match {
                case Some(render) => render.render(view, context)
                case None => throw new RuntimeException(s"Cannot find render for ${view.getClass}")
              }
            } else {
              if (null != contentNegotiationManager) {
                val mimeTypes = contentNegotiationManager.resolve(context.request).iterator
                var serializer: Serializer = null
                var mimeType: MimeType = null
                while (mimeTypes.hasNext && serializer == null) {
                  mimeType = mimeTypes.next()
                  serializer = serializerManager.getSerializer(mimeType)
                }
                if (null != serializer) {
                  val response = context.response
                  val request = context.request
                  response.setCharacterEncoding("UTF-8")
                  response.setContentType(mimeType.toString + "; charset=UTF-8")
                  val params = new collection.mutable.HashMap[String, Any]
                  val enum = request.getAttributeNames
                  while (enum.hasMoreElements()) {
                    val attr = enum.nextElement()
                    params.put(attr, request.getAttribute(attr))
                  }
                  params ++= context.params
                  serializer.serialize(result.asInstanceOf[AnyRef], response.getOutputStream, params.toMap)
                }
              }
            }
          } finally {
            postHandle(interceptors, context, lastInterceptorIndex)
          }
        }
      }
    } else {
      postHandle(interceptors, context, lastInterceptorIndex)
    }
  }

  def preHandle(interceptors: Array[Interceptor], context: ActionContext): Int = {
    val request = context.request
    val response = context.response
    var i = 0
    while (i < interceptors.length) {
      val interceptor = interceptors(i)
      if (!interceptor.preInvoke(request, response)) return i - 1
      i += 1
    }
    i - 1
  }

  def postHandle(interceptors: Array[Interceptor], context: ActionContext, lastInterceptorIndex: Int): Unit = {
    val request = context.request
    val response = context.response
    var i = lastInterceptorIndex
    while (i >= 0) {
      val interceptor = interceptors(i)
      interceptor.postInvoke(request, response)
      i -= 1
    }
  }
}