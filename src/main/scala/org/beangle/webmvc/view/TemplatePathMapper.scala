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

package org.beangle.webmvc.view

import org.beangle.commons.lang.Strings.{isNotEmpty, unCamel, uncapitalize}
import org.beangle.commons.lang.annotation.{description, spi}
import org.beangle.webmvc.config.Profile
import org.beangle.webmvc.config.Profile.{/, FULL_VIEWPATH, SEO_VIEWPATH, SIMPLE_VIEWPATH}

@spi
trait TemplatePathMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def map(className: String, viewName: String, profile: Profile): String
}

@description("缺省的模板路径映射器")
class DefaultTemplatePathMapper extends TemplatePathMapper {

  /**
   * 查询control对应的view的名字(没有后缀)
   */
  def map(className: String, viewName: String, profile: Profile): String = {
    if (isNotEmpty(viewName) && viewName.charAt(0) == /) viewName
    else {
      val buf = new StringBuilder()
      if (profile.viewPathStyle.equals(FULL_VIEWPATH)) {
        buf.append(/)
        buf.append(getFullPath(className, profile.actionSuffix))
      } else if (profile.viewPathStyle.equals(SIMPLE_VIEWPATH)) {
        buf.append(profile.viewPath)
        // 添加中缀路径
        buf.append(profile.getMatched(className))
      } else if (profile.viewPathStyle.equals(SEO_VIEWPATH)) {
        buf.append(profile.viewPath)
        buf.append(unCamel(profile.getMatched(className)))
      } else {
        throw new RuntimeException(profile.viewPathStyle + " was not supported")
      }
      // add method mapping path
      buf.append(/)

      buf.append(viewName)
      buf.toString()
    }
  }

  /**
   * 取得类对应的全路经，仅仅把类名第一个字母小写。
   */
  private def getFullPath(className: String, postfix: String): String = {
    val afterLastDotIdx = className.lastIndexOf('.') + 1
    val infix = new StringBuilder(className.substring(0, afterLastDotIdx))
    infix.append(uncapitalize(className.substring(afterLastDotIdx, className.length - postfix.length)))
    // 将.替换成/
    Range(0, infix.length) foreach { i =>
      if (infix.charAt(i) == '.') infix.setCharAt(i, '/')
    }
    infix.toString
  }
}
