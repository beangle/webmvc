package org.beangle.webmvc.struts2.dispatcher

import java.io.File
import org.apache.struts2.ServletActionContext
import org.apache.struts2.dispatcher.mapper.{ ActionMapper, ActionMapping, DefaultActionMapper }
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper
import org.beangle.commons.lang.Arrays.{ isBlank, isEmpty }
import org.beangle.commons.text.i18n.spi.{ TextBundleRegistry, TextFormater }
import com.opensymphony.xwork2.config.ConfigurationManager
import com.opensymphony.xwork2.inject.Inject
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.spi.context.LocaleResolver
import org.beangle.webmvc.spi.dispatch.RequestMapper
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.commons.text.i18n.impl.DefaultTextFormater
import org.beangle.webmvc.context.ActionTextResource
import org.beangle.commons.text.i18n.impl.DefaultTextBundleRegistry
import org.beangle.webmvc.context.ActionTextResourceProvider

class ConventionActionMapper extends DefaultActionMapper with ActionMapper {

  @Inject
  var resolver: RequestMapper = _

  val textResourceProvider = new ActionTextResourceProvider(new DefaultTextBundleRegistry(), new DefaultTextFormater)

  textResourceProvider.init()

  @Inject
  var localeResolver: LocaleResolver = _
  /**
   * reserved method parameter
   */
  override def getMapping(request: HttpServletRequest, configManager: ConfigurationManager): ActionMapping = {
    resolver.resolve(request) match {
      case Some(m) =>
        val response = ServletActionContext.getResponse
        val context = request match {
          case mp: MultiPartRequestWrapper => ActionContextHelper.build(request, response, localeResolver, m, getUploads(mp))
          case _ => ActionContextHelper.build(request, response, localeResolver, m)
        }
        ContextHolder.contexts.set(context)
        context.textResource = textResourceProvider.getTextResource(context.locale)

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