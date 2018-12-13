package org.mj.akka.actor.clustering

import akka.actor.{Actor, ActorLogging}
import org.mj.akka.actor.clustering.Messages.DoSomeWorkRouted

import scala.concurrent.{ExecutionContext, Future}

class WorkRouter extends Actor with ActorLogging {
  private implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case w: DoSomeWorkRouted =>
      val curSender = sender
      Future {
        val workResult = Decisions.workingOnItem(w.groupId, w.workItem)
        log.info(s"WorkRouter g-${w.groupId.id}_i-${w.workItem.id}:r${workResult.value} by actor ${self.path.name}") // log.debug("Working on workGroup {} for workItem {}: {} by {}", workGroup.id, workItem.id, workResult.value, self.path)
        curSender ! workResult
      }
  }
}
