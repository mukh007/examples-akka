package org.mj.akka.actor.example

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging

object ActorHierarchyApp extends App with LazyLogging {
  private val as = ActorSystem("ActorHierarchyApp")
  val actor = as.actorOf(Props[ParentActor], "ParentActor1")

  val actor2 = as.actorOf(Props[ParentActor], "ParentActor2")

  logger.info("Starting...")

  actor ! CreateChild
  actor ! SignalChildren(1)
  actor ! CreateChild
  actor ! CreateChild
  actor ! SignalChildren(2)

  actor2 ! CreateChild
  val child0 = as.actorSelection("/user/ParentActor2/ChildActor-0")
  child0 ! PrintSignal(3)

  logger.info("Finished...")

  Thread.sleep(2000)
  as.terminate()
}

case object CreateChild

case class SignalChildren(order: Int)

case class PrintSignal(order: Int)

class ParentActor extends Actor with LazyLogging {
  private var count = 0

  override def receive: Receive = {
    case CreateChild =>
      logger.debug(s"Received type CreateChild")
      context.actorOf(Props[ChildActor], s"ChildActor-$count")
      count += 1
    case SignalChildren(n) =>
      logger.debug(s"Received type SignalChildren")
      context.children.foreach(_ ! PrintSignal(n))
  }
}

class ChildActor extends Actor with LazyLogging {
  override def receive: Receive = {
    case PrintSignal(n) =>
      logger.debug(s"Received type PrintSignal #$n : $self")
  }
}