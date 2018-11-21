package org.mj.akka.actor.example

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._

object ActorSchedulerApp extends App with LazyLogging {
  private val as = ActorSystem("ActorSchedulerApp")
  private implicit val es = as.dispatcher
  val actor = as.actorOf(Props[SchedulerActor], "SchedulerActor")
  logger.info("Starting...")

  actor ! Count
  as.scheduler.scheduleOnce(1.second)(actor ! Count)
  val can = as.scheduler.schedule(0.millis, 100.millis, actor, Count)

  logger.info("Terminating...")
  Thread.sleep(2000)
  can.cancel()
  as.terminate()

  case object Count

  class SchedulerActor extends Actor with LazyLogging {
    var count = 0

    override def receive: Receive = {
      case Count =>
        count += 1
        logger.info(s"Count = $count for $self")
    }
  }

}
