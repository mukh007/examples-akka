package org.mj.akka.actor.clustering.api

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import akka.io.IO
import akka.pattern.ask
import org.mj.akka.actor.clustering.Messages._
import spray.can.Http
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.concurrent.ExecutionContext

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class RestInterface(decider: ActorRef, exposedPort: Int) extends Actor with HttpServiceBase with ActorLogging {
  private implicit val system: ActorSystem = context.system
  private implicit val ec: ExecutionContext = context.dispatcher

  def receive: Receive = runRoute(route)

  private val route: Route = {
    path("requestId" / IntNumber / "group" / Segment / "item" / Segment) { (requestId, group, item) =>
      get {
        complete {
          log.info(s"Request $requestId for group $group and item $item")
          val junction = WorkGroup(group)
          val container = WorkItem(item)
          decider.ask(WhereShouldIGo(junction, container))(5 seconds).mapTo[WorkResult]
        }
      }
    }
  }

  IO(Http) ! Http.Bind(self, interface = "0.0.0.0", port = exposedPort)
}
