package org.mj.akka.actor.clustering

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import org.mj.akka.actor.clustering.Messages._

import scala.concurrent.{ExecutionContext, Future}

object ShardingDecider {
  def name = "ShardingDecider"

  def props: Props = Props[ShardingDecider]

  // Sharding logic goes here
  def extractShardId: ExtractShardId = {
    case DoSomeWork(workGroup, _) => (workGroup.id.toInt % 2).toString
  }

  // Routing logic goes here
  def extractEntityId: ExtractEntityId = {
    case msg@DoSomeWork(workGroup, _) => (workGroup.id.toString, msg)
  }
}

class ShardingDecider extends Actor with ActorLogging {
  private implicit val ec: ExecutionContext = context.dispatcher

  def receive: Receive = {
    case DoSomeWork(workGroup, workItem) =>
      val curSender = sender
      Future {
        val workResult = Decisions.workingOnItem(workGroup, workItem)
        log.info(s"Work g-${workGroup.id}_i-${workItem.id}:r${workResult.value} by actor ${self.path.name}") // log.debug("Working on workGroup {} for workItem {}: {} by {}", workGroup.id, workItem.id, workResult.value, self.path)
        curSender ! workResult
      }
  }
}
