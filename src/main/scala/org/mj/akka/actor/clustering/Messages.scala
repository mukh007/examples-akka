package org.mj.akka.actor.clustering

import spray.json.DefaultJsonProtocol._

object Messages {
  case class WorkGroup(id: String)

  case class WorkItem(id: String)

  case class WhereShouldIGo(group: WorkGroup, item: WorkItem)

  case class WorkResult(value: String)

  object WorkResult {
    implicit val goJson = jsonFormat1(WorkResult.apply)
  }
}
