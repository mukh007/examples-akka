package org.mj.akka.actor.clustering.api

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import akka.io.IO
import akka.pattern._
import akka.util.Timeout
import org.mj.akka.actor.clustering.Messages._
import spray.can.Http
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.ExecutionContext

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class RestInterface(decider: ActorRef, portId: Int) extends Actor with HttpServiceBase with ActorLogging {
  private implicit val system: ActorSystem = context.system
  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val to: Timeout = 5.second

  def receive: Receive = runRoute(route)

  private val route: Route = {
    path("shard-requestId" / IntNumber / "group" / Segment / "item" / Segment) {
      (requestId, group, item) =>
        get {
          complete {
            log.info(s"Request $requestId for group $group and item $item")
            val workGroup = WorkGroup(group)
            val workItem = WorkItem(item)
            val work = DoSomeWork(workGroup, workItem)
            //val work = DoSomeWorkRouted(workGroup, workItem)
            val workResult = (decider ? work).mapTo[WorkResult]
            workResult
          }
        }
    } ~
    path("route-requestId" / IntNumber / "group" / Segment / "item" / Segment) {
      (requestId, group, item) =>
        get {
          complete {
            log.info(s"Request $requestId for group $group and item $item")
            val workGroup = WorkGroup(group)
            val workItem = WorkItem(item)
            val work = DoSomeWorkRouted(workGroup, workItem)
            //val work = DoSomeWorkRouted(workGroup, workItem)
            val workResult = (decider ? work).mapTo[WorkResult]
            workResult
          }
        }
    }
  }

  IO(Http) ! Http.Bind(self, interface = "0.0.0.0", port = portId)
}
