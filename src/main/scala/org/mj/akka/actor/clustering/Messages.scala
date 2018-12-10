package org.mj.akka.actor.clustering

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

object Messages {
  case class WorkGroup(id: String)

  case class WorkItem(id: String)

  case class DoSomeWork(groupId: WorkGroup, workItem: WorkItem)

  case class WorkResult(value: String)

  object WorkResult {
    implicit val goJson: RootJsonFormat[WorkResult] = jsonFormat1(WorkResult.apply)
  }
}
