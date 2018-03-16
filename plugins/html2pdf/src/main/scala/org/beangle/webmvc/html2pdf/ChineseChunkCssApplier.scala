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
package org.beangle.webmvc.html2pdf
import com.itextpdf.tool.xml.css.apply.ChunkCssApplier
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.tool.xml.Tag
import com.itextpdf.text.Font
import ChineseChunkCssApplier._
import com.itextpdf.text.FontProvider

object ChineseChunkCssApplier {
  val basefont = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED)

  def isChinese(c: Char): Boolean = {
    val ub = Character.UnicodeBlock.of(c)
    (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
      || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
      || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
      || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
      || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
      || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS)
  }

  def isChinese(strName: String): Boolean = {
    val ch = strName.toCharArray()
    (0 until ch.length).exists(i => isChinese(ch(i)))
  }
}

/**
 * @author chaostone
 */
class ChineseChunkCssApplier(fontProvider: FontProvider) extends ChunkCssApplier(fontProvider) {

  override def applyFontStyles(t: Tag): Font = {
    val f = super.applyFontStyles(t)
    if (null != basefont && (null == f.getBaseFont())) {
      new Font(basefont, f.getSize(), f.getStyle(), f.getColor())
    } else {
      f
    }
  }
}
