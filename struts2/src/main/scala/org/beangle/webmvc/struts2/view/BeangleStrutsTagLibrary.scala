package org.beangle.webmvc.struts2.view

import org.apache.struts2.views.TagLibraryModelProvider
import org.beangle.webmvc.view.tag.BeangleTagLibrary

import com.opensymphony.xwork2.util.ValueStack

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

class BeangleStrutsTagLibrary extends BeangleTagLibrary with TagLibraryModelProvider {

  override def getModels(stack: ValueStack, req: HttpServletRequest, res: HttpServletResponse): Object = {
      super.getModels(req, res)
  }
}