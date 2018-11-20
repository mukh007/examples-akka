package org.mj.akka.actor.example

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging

object SimpleActorApp extends App with LazyLogging {
  private val as = ActorSystem("SimpleActorApp")
  val simpleActor = as.actorOf(Props[AskActor], "SimpleActor")

  logger.info("Starting...")
  simpleActor ! "Hello"
  simpleActor ! 42
  simpleActor ! 'a'
  logger.info("Finished...")

  as.terminate()
}

class SimpleActor extends Actor with LazyLogging {
  override def receive: Receive = {
    case i: Int => logger.info(s"Received type int : $i")
    case s: String => logger.info(s"Received type string : $s")
    case a: Any => logger.info(s"Received type any : $a")
  }

  def foo(): Unit = {
    logger.info("Inside foo")
  }
}