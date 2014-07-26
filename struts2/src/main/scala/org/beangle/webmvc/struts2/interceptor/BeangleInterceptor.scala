package org.beangle.webmvc.struts2.interceptor

import java.io.File

import org.apache.struts2.StrutsStatics.{ HTTP_REQUEST, HTTP_RESPONSE }
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper
import org.beangle.commons.lang.Arrays.{ isBlank, isEmpty }
import org.beangle.webmvc.context.{ ActionContext, ContextHolder }

import com.opensymphony.xwork2.{ ActionContext => XworkContext, ActionInvocation }
import com.opensymphony.xwork2.interceptor.AbstractInterceptor

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

@SerialVersionUID(8451445989084058881L)
class BeangleInterceptor extends AbstractInterceptor {

  @throws(classOf[Exception])
  override def intercept(invocation: ActionInvocation): String = {
    val ctx = invocation.getInvocationContext
    val request = ctx.get(HTTP_REQUEST).asInstanceOf[HttpServletRequest]
    val response = ctx.get(HTTP_RESPONSE).asInstanceOf[HttpServletResponse]
    val context = new ActionContext(request, response, getParams(request))
    ContextHolder.contexts.set(context)
    val result = invocation.invoke()
    val flash = context.flash
    if (null != flash) flash.nextToNow()
    result
  }

  private def getParams(request: HttpServletRequest): Map[String, Any] = {
    val context = ContextHolder.context
    val itor = XworkContext.getContext().getParameters().entrySet().iterator()
    val paramsBuilder = new collection.mutable.HashMap[String, Any]
    while (itor.hasNext) {
      val entry = itor.next()
      paramsBuilder.put(entry.getKey(), entry.getValue)
    }
    request match {
      case mp: MultiPartRequestWrapper => paramsBuilder ++= getUploads(mp)
      case _ =>
    }
    paramsBuilder.toMap
  }

  def getUploads(mp: MultiPartRequestWrapper): Map[String, Any] = {
    val paramsBuilder = new collection.mutable.HashMap[String, Any]
    // bind allowed Files
    val fileParameterNames = mp.getFileParameterNames
    if (null == fileParameterNames) return Map.empty

    while (fileParameterNames.hasMoreElements()) {
      // get the value of this input tag
      val inputName = fileParameterNames.nextElement()

      // get the content type
      val contentType = mp.getContentTypes(inputName)
      val fileName = mp.getFileNames(inputName)
      val files = mp.getFiles(inputName)

      if (!isBlank(contentType) && !isBlank(fileName) && !isEmpty(files)) {
        val acceptedFiles = new collection.mutable.ListBuffer[File]
        val acceptedContentTypes = new collection.mutable.ListBuffer[String]
        val acceptedFileNames = new collection.mutable.ListBuffer[String]
        val contentTypeName = inputName + "ContentType"
        val fileNameName = inputName + "FileName"

        for (index <- 0 until files.length) {
          acceptedFiles += files(index)
          acceptedContentTypes += contentType(index)
          acceptedFileNames += fileName(index)
        }
        paramsBuilder.put(inputName, acceptedFiles.toArray)
        paramsBuilder.put(contentTypeName, acceptedContentTypes.toArray)
        paramsBuilder.put(fileNameName, acceptedFileNames.toArray)
      }
    }
    paramsBuilder.toMap
  }
}