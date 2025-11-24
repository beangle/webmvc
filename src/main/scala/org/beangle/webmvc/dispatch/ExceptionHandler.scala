/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.dispatch

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.bean.Initializing
import org.beangle.commons.cdi.BindRegistry
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.{JVM, Primitives, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.web.servlet.http.accept.ContentNegotiationManager
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.execution.{BindException, Handler}

import java.io.{PrintWriter, StringWriter}
import java.time.LocalDateTime

trait ExceptionHandler {

  def handle(request: HttpServletRequest, response: HttpServletResponse, handler: Handler, ex: Exception): Unit
}

abstract class AbstractExceptionHandler extends ExceptionHandler, Initializing {

  var contentNegotiationManager: ContentNegotiationManager = _

  var devMode: Boolean = _

  var ignorePackages = Seq("at org.apache.tomcat.",
    "at org.apache.catalina.", "at org.apache.coyote.", "at java.base/", "...",
    "at org.beangle.webmvc.", "at scala.",
    "at jdk.", "at org.springframework.aop."
  )

  override def init(): Unit = {
    devMode = !devEnabled
  }

  def getErrorAttributes(request: HttpServletRequest, ex: Exception): collection.Map[String, Any] = {
    val attrs = Collections.newMap[String, Any]
    attrs.put("timestamp", LocalDateTime.now)
    attrs.put("status", 500)
    attrs.put("path", RequestUtils.getServletPath(request))
    if (null != ex) {
      attrs.put("message", ex.getMessage)
      attrs.put("exception", ex.getClass.getName)
      attrs.put("trace", getStackTrace(ex))
    }
    attrs
  }

  private def jsonValue(v: Any): String = {
    v match {
      case null => "\"\""
      case a: Array[_] => a.map(jsonValue(_)).mkString("[", ",", "]")
      case s: collection.Seq[_] => s.map(jsonValue(_)).mkString("[", ",", "]")
      case m: collection.Map[_, _] => toJson(m.asInstanceOf[collection.Map[String, Any]])
      case d: Any =>
        if (Primitives.isWrapperType(d.getClass) || d.getClass.isPrimitive) {
          String.valueOf(v)
        } else {
          s"\"${d.toString.trim()}\""
        }
    }
  }

  protected def toJson(attrs: collection.Map[String, Any]): String = {
    val sb = new StringBuilder
    sb.append("{")
    attrs.foreach { case (k, v) =>
      if k != "trace" || devMode then sb.append(s"\"${k}\":${jsonValue(v)},")
    }
    sb.deleteCharAt(sb.length() - 1)
    sb.append("}")
    sb.toString()
  }

  private def xmlValue(v: Any): String = {
    v match {
      case null => ""
      case a: Array[_] => a.map(xmlValue(_).replace("\n", "")).mkString("\n")
      case s: collection.Seq[_] => s.map(xmlValue(_).replace("\n", "")).mkString("\n")
      case m: collection.Map[_, _] =>
        m.map(e => s"<${e._1}>${xmlValue(e._2)}</${e._1}>").mkString("")
      case d: Any => escapeXml(d.toString)
    }
  }

  protected def toXml(attrs: collection.Map[String, Any]): String = {
    val sb = new StringBuilder
    sb.append("""<?xml version="1.0" encoding="UTF-8"?><error>""")
    attrs.foreach { case (k, v) =>
      if k != "trace" || devMode then sb.append(s"<${k}>${xmlValue(v)}</${k}>")
    }
    sb.append("</error>")
    sb.toString()
  }

  protected def toHtml(attrs: collection.Map[String, Any], request: HttpServletRequest): String = {
    // 2. 从 model 中获取错误上下文（由 DefaultErrorAttributes 提取）
    val status = attrs.getOrElse("status", null).asInstanceOf[Int] // 状态码（404/500）
    val error = attrs.getOrElse("error", null).asInstanceOf[String] // 错误描述（如 "Not Found"）
    val message = attrs.getOrElse("message", null).asInstanceOf[String] // 异常消息
    val path = attrs.getOrElse("path", null).asInstanceOf[String] // 请求路径
    val timestamp = attrs.get("timestamp").orNull // 时间戳
    val exception = attrs.getOrElse("exception", null).asInstanceOf[String] // 异常类名
    val trace = attrs.getOrElse("trace", null).asInstanceOf[Array[String]] // 堆栈信息（开发模式有）

    // 3. 动态拼接 HTML 内容
    val html = new StringBuilder()
    val isAjax = RequestUtils.isAjax(request)
    if (!isAjax) {
      html.append("<!DOCTYPE html>")
      html.append("<html lang=\"en\">")
      html.append("<head>")
      html.append("    <meta charset=\"UTF-8\">")
      html.append("    <title>Error ").append(status).append("</title>")
      html.append("</head>")
      html.append("<body>")
    }
    html.append("  <div id=\"error\" class=\"container\">")
    html.append("  <style>")
    // 4. 内置极简 CSS 样式（保证页面可读性）
    html.append("   #error { font-family: sans-serif;margin: 2rem }")
    html.append("   #error h1 { color: #dc3545 }")
    html.append("   #error p { margin:0.5em 0em }")
    html.append("   .details { margin-top: 1rem; padding: 1rem; background-color: #f8f9fa; border-radius: 0.25rem }")
    html.append("   .trace { font-family: monospace;white-space: pre-wrap;word-break: break-all }")
    html.append("  </style>")

    // 5. 渲染核心错误信息（状态码 + 错误描述）
    html.append("  <h1>White Error Page</h1>")
    html.append("  <p>This application has no explicit mapping for /error, so you are seeing this as a fallback.</p>")
    html.append("  <div class=\"details\">")
    html.append("    <p><strong>HTTP Status ").append(status).append("</strong> – ").append(error).append("</p>")

    // 6. 渲染额外细节（路径、时间戳、异常类名、堆栈）
    if (path != null) {
      html.append("      <p><strong>Path:</strong> ").append(escapeXml(path)).append("</p>")
    }
    if (timestamp != null) {
      html.append("      <p><strong>Timestamp:</strong> ").append(timestamp).append("</p>")
    }
    if (exception != null) {
      html.append("      <p><strong>Exception:</strong> ").append(escapeXml(exception)).append("</p>")
    }
    if (message != null) {
      html.append("      <p><strong>Message:</strong> ").append(escapeXml(message)).append("</p>")
    }
    if (devMode && trace != null && trace.length > 0) {
      html.append("      <p><strong>Stack Trace:</strong></p>")
      html.append("      <pre class=\"trace\">")
      trace foreach { line =>
        html.append(escapeXml(line)).append("") // 堆栈按行拼接
      }
      html.append("      </pre>")
    }

    html.append("  </div>")
    html.append("</div>")
    if (!isAjax) {
      html.append("</body>")
      html.append("</html>")
    }
    html.toString()
  }

  protected def getStackTrace(ex: Exception): Array[String] = {
    val stringWriter = new StringWriter()
    val printWriter = new PrintWriter(stringWriter)
    ex.printStackTrace(printWriter)
    printWriter.close()
    Strings.split(stringWriter.toString, "\n").filter(x => ignorePackages.forall(!x.contains(_)))
  }

  @deprecated("using EnvProfile.isDevMode")
  final def devEnabled: Boolean = {
    val profiles = System.getProperty(BindRegistry.ProfileProperty)
    val enabled = null != profiles && Strings.split(profiles, ",").toSet.contains("dev")
    enabled || JVM.isDebugMode
  }

  private def escapeXml(value: String): String = {
    if (value == null) return ""
    value.replace("&", "&amp").replace("<", "&lt").replace(">", "&gt").replace("\"", "&quot").replace("'", "&#39")
  }
}

class DefaultExceptionHandler extends AbstractExceptionHandler, Logging {
  def handle(request: HttpServletRequest, response: HttpServletResponse, handler: Handler, ex: Exception): Unit = {
    val attrs = getErrorAttributes(request, ex)
    response.setStatus(attrs.getOrElse("status", 500).asInstanceOf[Int])
    response.setCharacterEncoding("UTF-8")
    if (null != ex && !ex.isInstanceOf[BindException]) {
      logError(attrs, ex)
      report(attrs, ex)
    }
    val mediaType = contentNegotiationManager.resolve(request).headOption.map(_.toString).getOrElse("text/html")
    response.setContentType(mediaType)
    if (mediaType.contains("json")) {
      response.getWriter.write(toJson(attrs))
    } else if (mediaType.contains("xml")) {
      response.getWriter.write(toXml(attrs))
    } else {
      response.getWriter.write(toHtml(attrs, request))
    }
  }

  private def logError(attrs: collection.Map[String, Any], ex: Exception): Unit = {
    attrs.get("trace") foreach { trace =>
      logger.error(trace.asInstanceOf[Array[String]].mkString("\n"))
    }
  }

  protected def report(attrs: collection.Map[String, Any], ex: Exception): Unit = {
  }
}
