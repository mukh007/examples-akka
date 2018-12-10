package org.mj.akka.actor.clustering

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.routing.FromConfig
import org.mj.akka.actor.clustering.Messages._

import scala.concurrent.{ExecutionContext, Future}

object ShardingDecider {
  def name = "ShardingDecider"

  def props: Props = Props[ShardingDecider]

  // Sharding logic goes here
  def extractShardId: ExtractShardId = {
    case DoSomeWork(workGroup, _) => (workGroup.id.toInt % 2).toString

    case DoSomeWorkRouted(workGroup, _) => (workGroup.id.toInt % 2).toString
  }

  // Routing logic goes here
  def extractEntityId: ExtractEntityId = {
    case msg@DoSomeWork(workGroup, _) => (workGroup.id.toString, msg)

    case msg@DoSomeWorkRouted(workGroup, _) => (workGroup.id.toString, msg)
  }
}

class ShardingDecider extends Actor with ActorLogging {
  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val sys: ActorSystem = context.system

  //val router: ActorRef = sys.actorOf(FromConfig.props(Props[WorkRouter]), "router1")
  //val router: ActorRef = sys.actorOf(Props[WorkRouter].withRouter(FromConfig()), "router1")

  def receive: Receive = {
    case DoSomeWork(workGroup, workItem) =>
      val curSender = sender
      Future {
        val workResult = Decisions.workingOnItem(workGroup, workItem)
        log.info(s"Work g-${workGroup.id}_i-${workItem.id}:r${workResult.value} by actor ${self.path.name}") // log.debug("Working on workGroup {} for workItem {}: {} by {}", workGroup.id, workItem.id, workResult.value, self.path)
        curSender ! workResult
      }
    case work: DoSomeWorkRouted =>
      val curSender = sender
      Future {
        val workResult = Decisions.workingOnItem(work.groupId, work.workItem)
        log.info(s"Work g-${work.groupId.id}_i-${work.workItem.id}:r${workResult.value} by actor ${self.path.name}")
        curSender ! workResult
      }
    //      router ! work
    //    case workRes: WorkResult =>
    //      sender ! workRes
  }
}


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