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

import java.io.{File, IOException, InputStream}
import java.net.{JarURLConnection, URL, URLConnection}

/**
 * Wraps a {@link URL}, and implements methods required for a typical template source.
 */
class URLTemplateSource(val url: URL, useCaches: Option[Boolean]) {

  private var conn: URLConnection = null
  private var inputStream: InputStream = null

  this.conn = url.openConnection
  useCaches foreach { a =>
    conn.setUseCaches(a)
  }

  override def equals(o: Any): Boolean = {
    o match {
      case u: URLTemplateSource => u.url == this.url
      case _ => false
    }
  }

  override def hashCode: Int = {
    url.hashCode
  }

  override def toString: String = {
    url.toString
  }

  def lastModified: Long = {
    conn match {
      case jarUrlConn: JarURLConnection =>
        // There is a bug in sun's jar url connection that causes file handle leaks when calling getLastModified()
        // (see https://bugs.openjdk.java.net/browse/JDK-6956385).
        // Since the time stamps of jar file contents can't vary independent from the jar file timestamp, just use
        // the jar file timestamp
        val jarURL = jarUrlConn.getJarFileURL
        if (jarURL.getProtocol == "file") { // Return the last modified time of the underlying file - saves some opening and closing
          new File(jarURL.getFile).lastModified
        } else { // Use the URL mechanism
          var jarConn: URLConnection = null
          try {
            jarConn = jarURL.openConnection
            jarConn.getLastModified
          } catch {
            case _: IOException => -(1)
          } finally {
            try if (jarConn != null) {
              jarConn.getInputStream.close()
            } catch {
              case _: IOException =>
            }
          }
        }
      case _ =>
        val lastModified = conn.getLastModified
        if (lastModified == -(1L) && url.getProtocol == "file") {
          // Hack for obtaining accurate last modified time for
          // URLs that point to the local file system. This is fixed
          // in JDK 1.4, but prior JDKs returns -1 for file:// URLs.
          new File(url.getFile).lastModified
        } else {
          lastModified
        }
    }
  }

  def getInputStream: InputStream = {
    if (inputStream != null) {
      try inputStream.close()
      catch {
        case _: IOException => // Ignore
      }
      this.conn = url.openConnection
    }
    inputStream = conn.getInputStream
    inputStream
  }

  @throws[IOException]
  def close(): Unit = {
    try if (inputStream != null) {
      inputStream.close()
    } else {
      conn.getInputStream.close()
    } finally {
      inputStream = null
      conn = null
    }
  }

}
