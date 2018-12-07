package org.mj.akka.actor.clustering

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import org.mj.akka.actor.clustering.Messages._

import scala.concurrent.{ExecutionContext, Future}

object ShardingDecider {
  def name = "ShardingDecider"

  def props: Props = Props[ShardingDecider]

  def extractShardId: ExtractShardId = {
    case WhereShouldIGo(group, _) => (group.id.toInt % 2).toString
  }

  def extractEntityId: ExtractEntityId = {
    case msg@WhereShouldIGo(group, _) => (group.id.toString, msg)
  }
}

class ShardingDecider extends Actor with ActorLogging {
  private implicit val ec: ExecutionContext = context.dispatcher

  def receive: Receive = {
    case WhereShouldIGo(group, item) =>
      val curSender = sender
      Future {
        val result = Decisions.whereShouldContainerGo(group, item)
        log.info("Working on group {} for item {}: {} by {}", group.id, item.id, result.value, self.path)
        curSender ! result
      }
  }
}
