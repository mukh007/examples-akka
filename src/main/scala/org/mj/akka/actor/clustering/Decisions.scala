package org.mj.akka.actor.clustering

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.mj.akka.actor.clustering.Messages.{WorkGroup, WorkItem, WorkResult}

object Decisions {
  private val timePattern = "ss.SSSZ" // "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  private val timeFormat: DateTimeFormatter = DateTimeFormat.forPattern(timePattern)

  def workingOnItem(group: WorkGroup, item: WorkItem): WorkResult = {
    Thread.sleep(5) // just to simulate resource hunger
    WorkResult(s"w_${group.id}:${item.id}@${timeFormat.print(DateTime.now)}")
  }
}
