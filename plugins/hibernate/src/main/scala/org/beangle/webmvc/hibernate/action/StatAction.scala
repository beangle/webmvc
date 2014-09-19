package org.beangle.webmvc.hibernate.action

import java.{ util => ju }
import scala.collection.mutable
import org.beangle.commons.bean.Initializing
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.webmvc.api.action.ActionSupport
import org.hibernate.SessionFactory
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.annotation.action

@description("Hibernate运行/缓存统计查看器")
@action("stat/{session_factory_id}")
class StatAction extends AbstractAction {

  var activation: ju.Date = null
  var deactivation: ju.Date = null

  def index(): String = {
    val sessionFactory = getSessionFactory()
    val statistics = sessionFactory.getStatistics()
    val lastUpdate = new ju.Date()

    val generalStatistics = new mutable.ListBuffer[Long]
    val action = get("do", "")
    val info = new StringBuilder(512)

    if ("activate".equals(action) && !statistics.isStatisticsEnabled()) {
      statistics.setStatisticsEnabled(true)
      activation = new ju.Date()
      deactivation = null
      info.append("Statistics enabled\n")
    } else if ("deactivate".equals(action) && statistics.isStatisticsEnabled()) {
      statistics.setStatisticsEnabled(false)
      deactivation = new ju.Date()
      info.append("Statistics disabled\n")
    } else if ("clear".equals(action)) {
      activation = null
      deactivation = null
      statistics.clear()
      generalStatistics.clear()
      info.append("Statistics cleared\n")
    }

    if (info.length() > 0) addMessage(info.toString())

    val active = statistics.isStatisticsEnabled()
    if (active) {
      generalStatistics += statistics.getConnectCount()
      generalStatistics += statistics.getFlushCount()

      generalStatistics += statistics.getPrepareStatementCount()
      generalStatistics += statistics.getCloseStatementCount()

      generalStatistics += statistics.getSessionCloseCount()
      generalStatistics += statistics.getSessionOpenCount()

      generalStatistics += statistics.getTransactionCount()
      generalStatistics += statistics.getSuccessfulTransactionCount()
      generalStatistics += statistics.getOptimisticFailureCount()
    }
    put("active", active)
    put("lastUpdate", lastUpdate)
    if (null != activation) {
      if (null != deactivation) {
        put("duration", deactivation.getTime() - activation.getTime())
      } else {
        put("duration", lastUpdate.getTime() - activation.getTime())
      }
    }
    put("activation", activation)
    put("deactivation", deactivation)
    put("generalStatistics", generalStatistics)
    put("statistics",statistics)
    return forward()
  }

  def entity(): String = {
    val statistics = getSessionFactory().getStatistics()
    put("statistics", statistics)
    return forward()
  }

  def query(): String = {
    val statistics = getSessionFactory().getStatistics()
    put("statistics", statistics)
    return forward("queryCache")
  }

  def collection(): String = {
    val statistics = getSessionFactory().getStatistics()
    put("statistics", statistics)
    return forward()
  }

  def cache(): String = {
    val statistics = getSessionFactory().getStatistics()
    put("statistics", statistics)
    return forward()
  }
}