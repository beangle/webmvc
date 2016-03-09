/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.html2pdf

import java.io.{ OutputStream, StringReader }
import org.beangle.commons.collection.Collections
import com.itextpdf.text.{ Document, PageSize, Rectangle }
import com.itextpdf.text.pdf.{ BaseFont, PdfWriter }
import com.itextpdf.tool.xml.{ XMLWorker, XMLWorkerFontProvider }
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver
import com.itextpdf.tool.xml.html.{ CssAppliersImpl, Tags }
import com.itextpdf.tool.xml.parser.XMLParser
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline
import com.itextpdf.tool.xml.pipeline.html.{ HtmlPipeline, HtmlPipelineContext }
import ITextPdfReporter.Rectangles
import org.beangle.commons.io.IOs
import java.io.FileInputStream
import java.io.FileOutputStream
import com.itextpdf.tool.xml.css.CssFilesImpl
import com.itextpdf.tool.xml.XMLWorkerHelper
import java.io.Reader
import org.beangle.commons.lang.ClassLoaders
/**
 * @author chaostone
 */
object ITextPdfReporter {
  val Rectangles = Collections.newMap[String, Rectangle]
  classOf[PageSize].getFields foreach { f =>
    Rectangles.put(f.getName, f.get(f).asInstanceOf[Rectangle])
  }
  def main(args: Array[String]) {
    val reporter = new ITextPdfReporter
    val context = new ReportContext
    val html = IOs.readString(new FileInputStream("/home/chaostone/openurp/site/_site/model/partition.html"))
    val os = new FileOutputStream("/home/chaostone/openurp/site/_site/model.pdf")
    reporter.generate(new StringReader(html), context, os)
    os.close()
  }
}

class ITextPdfReporter {

  def generate(reader: Reader, context: ReportContext, os: OutputStream): Unit = {
    val rectangle = context.datas.get("page-size") match {
      case Some(pageSize) => Rectangles.get(pageSize.toString).getOrElse(PageSize.A4)
      case None           => PageSize.A4
    }
    val document = new Document(rectangle)
    val writer = PdfWriter.getInstance(document, os)
    document.open()
    parseXHtml(writer, document, reader)
    document.close()
  }

  private def parseXHtml(writer: PdfWriter, doc: Document, reader: Reader): Unit = {
    val fontProvider = new XMLWorkerFontProvider()

    val cssFiles = new CssFilesImpl()
    cssFiles.add(XMLWorkerHelper.getCSS(classOf[XMLWorkerHelper].getResourceAsStream("/default.css")))
    
    val cssResolver = new StyleAttrCSSResolver(cssFiles)

    val cssAppliers = new CssAppliersImpl(fontProvider)
    cssAppliers.setChunkCssAplier(new ChineseChunkCssApplier(fontProvider))

    val hpc = new HtmlPipelineContext(cssAppliers)
    hpc.setAcceptUnknown(true).autoBookmark(true).setTagFactory(Tags.getHtmlTagProcessorFactory())
    val pipeline = new CssResolverPipeline(cssResolver, new HtmlPipeline(hpc, new PdfWriterPipeline(
      doc, writer)))

    val worker = new XMLWorker(pipeline, true)
    val p = new XMLParser()
    p.addListener(worker)
    p.parse(reader)
  }

}