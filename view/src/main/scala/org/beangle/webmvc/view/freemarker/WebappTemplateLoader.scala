/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.view.freemarker

import java.io._
import java.net.{MalformedURLException, URL}

import freemarker.cache.TemplateLoader
import freemarker.log.Logger
import freemarker.template.utility.{NullArgumentException, StringUtil}
import jakarta.servlet.ServletContext

/**
 * A {@link TemplateLoader} that uses streams reachable through {@link ServletContext# getResource ( String )} as its source
 * of templates.
 */
object WebappTemplateLoader {

  def apply(servletContext: ServletContext): WebappTemplateLoader = {
    apply(servletContext, "/")
  }

  def apply(servletContext: ServletContext, subdirPath: String): WebappTemplateLoader = {
    NullArgumentException.check("servletContext", servletContext)
    NullArgumentException.check("subdirPath", subdirPath)
    var subDir = subdirPath.replace('\\', '/')
    if (!subDir.endsWith("/")) subDir += "/"
    if (!subDir.startsWith("/")) subDir = "/" + subDir
    new WebappTemplateLoader(servletContext, subDir)
  }
}

class WebappTemplateLoader(val servletContext: ServletContext, val subdirPath: String) extends TemplateLoader {
  private val LOG = Logger.getLogger("freemarker.cache")
  var urlConnectionUsesCaches = false
  var attemptFileAccess = true

  @throws[IOException]
  def findTemplateSource(name: String): Any = {
    val fullPath = subdirPath + name
    if (attemptFileAccess) { // First try to open as plain file (to bypass servlet container resource caches).
      try {
        val realPath = servletContext.getRealPath(fullPath)
        if (realPath != null) {
          val file = new File(realPath)
          if (file.canRead && file.isFile) return file
        }
      } catch {
        case _: SecurityException =>
        // ignore
      }
    }
    // If it fails, try to open it with servletContext.getResource.
    var url: URL = null
    try url = servletContext.getResource(fullPath)
    catch {
      case e: MalformedURLException =>
        LOG.warn("Could not retrieve resource " + StringUtil.jQuoteNoXSS(fullPath), e)
        return null
    }
    if (url == null) null
    else new URLTemplateSource(url, Some(urlConnectionUsesCaches))
  }

  def getLastModified(templateSource: Any): Long = {
    templateSource match {
      case f: File => f.lastModified()
      case u: URLTemplateSource => u.lastModified
    }
  }

  @throws[IOException]
  def getReader(templateSource: Any, encoding: String): Reader = {
    templateSource match {
      case f: File => new InputStreamReader(new FileInputStream(f))
      case u: URLTemplateSource => new InputStreamReader(u.getInputStream, encoding)
    }
  }

  @throws[IOException]
  def closeTemplateSource(templateSource: Any): Unit = {
    templateSource match {
      case u: URLTemplateSource => u.close()
      case _ =>
    }
  }


  /**
   * Show class name and some details that are useful in template-not-found errors.
   * @since 2.3.21
   */
  override def toString: String = {
    "(subdirPath=" + StringUtil.jQuote(subdirPath) + ", servletContext={contextPath=" + StringUtil.jQuote(getContextPath) + ", displayName=" + StringUtil.jQuote(servletContext.getServletContextName) + "})"
  }

  /** Gets the context path if we are on Servlet 2.5+, or else returns failure description string. */
  private def getContextPath: String = {
    servletContext.getContextPath
  }

}
