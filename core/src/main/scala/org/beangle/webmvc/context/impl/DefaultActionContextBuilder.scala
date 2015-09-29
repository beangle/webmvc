package org.beangle.webmvc.context.impl

import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.web.multipart.StandardMultipartResolver
import org.beangle.webmvc.api.context.{ ActionContext, ActionContextHolder }
import org.beangle.webmvc.context.{ ActionContextBuilder, ActionContextHelper, LocaleResolver }
import org.beangle.webmvc.execution.Handler
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.webmvc.context.ActionContextInitializer
import org.beangle.commons.inject.Container
import org.beangle.commons.bean.Initializing

/**
 * @author chaostone
 */
@description("缺省的ActionContext构建器")
class DefaultActionContextBuilder(container: Container) extends ActionContextBuilder with Initializing {

  var localeResolver: LocaleResolver = _

  var initializers: Iterable[ActionContextInitializer] = _

  override def init(): Unit = {
    initializers = container.getBeans(classOf[ActionContextInitializer]).values.toList
  }

  override def build(request: HttpServletRequest, response: HttpServletResponse,
    handler: Handler, params2: collection.Map[String, Any]): ActionContext = {

    val params = new collection.mutable.HashMap[String, Any]
    val paramIter = request.getParameterMap.entrySet.iterator
    while (paramIter.hasNext) {
      val paramEntry = paramIter.next
      val values = paramEntry.getValue
      if (values.length == 1) params.put(paramEntry.getKey, values(0))
      else params.put(paramEntry.getKey, values)
    }

    if (StandardMultipartResolver.isMultipart(request)) {
      params ++= StandardMultipartResolver.resolve(request)
    }

    params ++= params2

    val context = new ActionContext(request, response, localeResolver.resolve(request), params.toMap)
    context.temp(ActionContextHelper.HandlerAttribute, handler)
    ActionContextHolder.contexts.set(context)
    initializers foreach { i => i.init(context) }
    context
  }
}