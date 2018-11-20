package org.mj.akka.actor.example

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging

object ActorCountDownApp extends App with LazyLogging {
  private val as = ActorSystem("ActorCountDownApp")
  val actor1 = as.actorOf(Props[CountDownActor], "CountDownActor1")
  val actor2 = as.actorOf(Props[CountDownActor], "CountDownActor2")

  logger.info("Starting...")
  actor1 ! StartCounting(10, actor2)
  logger.info("Finished...")
}

case class StartCounting(value: Int, other: ActorRef)

case class CountDown(value: Int)

class CountDownActor extends Actor with LazyLogging {
  override def receive: Receive = {
    case StartCounting(value, other) =>
      logger.info(s"Received $value by $self")
      other ! CountDown(value - 1)
    case CountDown(value) =>
      logger.info(s"Received $value by $self")
      if (value > 0) {
        logger.info(s"Current value $value by $self")
        sender ! CountDown(value - 1)
      } else {
        logger.info(s"CountDown finished $value by $self terminating now.")
        context.system.terminate()
      }
  }
}