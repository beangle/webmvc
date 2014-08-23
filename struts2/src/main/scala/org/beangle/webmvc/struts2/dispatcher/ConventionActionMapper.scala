package org.beangle.webmvc.struts2.dispatcher

import java.io.File
import org.apache.struts2.ServletActionContext
import org.apache.struts2.dispatcher.mapper.{ ActionMapper, ActionMapping, DefaultActionMapper }
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper
import org.beangle.commons.lang.Arrays.{ isBlank, isEmpty }
import org.beangle.commons.text.i18n.{ DefaultTextBundleRegistry, DefaultTextFormater }
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.context.{ ActionContextHelper, LocaleResolver }
import org.beangle.webmvc.context.impl.ActionTextResourceProvider
import org.beangle.webmvc.dispatch.RequestMapper
import com.opensymphony.xwork2.config.ConfigurationManager
import com.opensymphony.xwork2.inject.Inject
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.helper.ContainerHelper
import org.beangle.commons.text.i18n.TextResourceProvider

class ConventionActionMapper extends DefaultActionMapper with ActionMapper {

  val mapper: RequestMapper = ContainerHelper.get.getBean(classOf[RequestMapper]).get

  val textResourceProvider = ContainerHelper.get.getBean(classOf[TextResourceProvider]).get

  val localeResolver = ContainerHelper.get.getBean(classOf[LocaleResolver]).get
  /**
   * reserved method parameter
   */
  override def getMapping(request: HttpServletRequest, configManager: ConfigurationManager): ActionMapping = {
    mapper.resolve(request) match {
      case Some(m) =>
        ActionContextHelper.build(request, ServletActionContext.getResponse, m, localeResolver, textResourceProvider)
        val am = new ActionMapping()
        val action = m.action
        am.setNamespace(action.namespace)
        am.setName(action.name)
        am.setMethod(m.action.method)
        am
      case None => null
    }
  }

  //  def getUploads(mp: MultiPartRequestWrapper): Map[String, Any] = {
  //    val paramsBuilder = new collection.mutable.HashMap[String, Any]
  //    // bind allowed Files
  //    val fileParameterNames = mp.getFileParameterNames
  //    if (null == fileParameterNames) return Map.empty
  //
  //    while (fileParameterNames.hasMoreElements()) {
  //      // get the value of this input tag
  //      val inputName = fileParameterNames.nextElement()
  //
  //      // get the content type
  //      val contentType = mp.getContentTypes(inputName)
  //      val fileName = mp.getFileNames(inputName)
  //      val files = mp.getFiles(inputName)
  //
  //      if (!isBlank(contentType) && !isBlank(fileName) && !isEmpty(files)) {
  //        val acceptedFiles = new collection.mutable.ListBuffer[File]
  //        val acceptedContentTypes = new collection.mutable.ListBuffer[String]
  //        val acceptedFileNames = new collection.mutable.ListBuffer[String]
  //        val contentTypeName = inputName + "ContentType"
  //        val fileNameName = inputName + "FileName"
  //
  //        for (index <- 0 until files.length) {
  //          acceptedFiles += files(index)
  //          acceptedContentTypes += contentType(index)
  //          acceptedFileNames += fileName(index)
  //        }
  //        paramsBuilder.put(inputName, acceptedFiles.toArray)
  //        paramsBuilder.put(contentTypeName, acceptedContentTypes.toArray)
  //        paramsBuilder.put(fileNameName, acceptedFileNames.toArray)
  //      }
  //    }
  //    paramsBuilder.toMap
  //  }
}