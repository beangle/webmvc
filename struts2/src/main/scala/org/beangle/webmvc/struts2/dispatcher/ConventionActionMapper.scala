package org.beangle.webmvc.struts2.dispatcher

import java.io.File
import org.apache.struts2.ServletActionContext
import org.apache.struts2.dispatcher.mapper.{ ActionMapper, ActionMapping, DefaultActionMapper }
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper
import org.beangle.commons.lang.Arrays.{ isBlank, isEmpty }
import org.beangle.webmvc.context.{ ActionContextBuilder, ContextHolder }
import org.beangle.webmvc.route.RequestMapper
import com.opensymphony.xwork2.config.ConfigurationManager
import com.opensymphony.xwork2.inject.Inject
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.text.i18n.spi.TextBundleRegistry
import org.beangle.webmvc.struts2.ActionTextResource
import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.text.i18n.impl.DefaultTextFormater
import com.opensymphony.xwork2.ActionContext

class ConventionActionMapper extends DefaultActionMapper with ActionMapper {

  @Inject
  var resolver: RequestMapper = _

  @Inject
  var registry: TextBundleRegistry = _

  val formater = new DefaultTextFormater
  /**
   * reserved method parameter
   */
  override def getMapping(request: HttpServletRequest, configManager: ConfigurationManager): ActionMapping = {
    resolver.resolve(request) match {
      case Some(m) =>
        val response = ServletActionContext.getResponse
        val context = request match {
          case mp: MultiPartRequestWrapper => ActionContextBuilder.build(request, response, m.params, getUploads(mp))
          case _ => ActionContextBuilder.build(request, response, m.params)
        }
        context.mapping = m
        context.textResource = new ActionTextResource(m.action.clazz, context.locale, registry, formater, ActionContext.getContext.getValueStack)

        ContextHolder.contexts.set(context)

        val am = new ActionMapping()
        val action = m.action
        am.setNamespace(action.namespace)
        am.setName(action.name)
        am.setMethod(m.action.method)
        am
      case None => null
    }
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