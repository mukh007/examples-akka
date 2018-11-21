package org.mj.akka.actor.example

import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}
import com.typesafe.scalalogging.LazyLogging

object ActorSupervisorApp extends App with LazyLogging {
  private val as = ActorSystem("ActorSupervisorApp")
  val actor = as.actorOf(Props[ParentActor], "ParentActor")

  logger.info("Starting...")

  actor ! CreateChild
  val child = as.actorSelection("/user/ParentActor/ChildActor-0")
  child ! DivideNumbers(4, 2)
  child ! DivideNumbers(4, 0)
  child ! DivideNumbers(4, 1)
  child ! BadStuff(new RuntimeException)

  Thread.sleep(2000)
  logger.info("Terminating...")
  as.terminate()

  case object CreateChild

  case class SignalChildren(order: Int)

  case class PrintSignal(order: Int)

  case class DivideNumbers(n: Int, d: Int)

  case class BadStuff(ex: Throwable)

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

    override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy(loggingEnabled = false) {
      case ae: ArithmeticException =>
        logger.debug(s"Handling Resume $ae in $self")
        Resume
      case ex: Exception =>
        logger.debug(s"Handling Restart $ex in $self")
        Restart
    }
  }

  class ChildActor extends Actor with LazyLogging {
    logger.info("Child created")
    override def receive: Receive = {
      case PrintSignal(n) =>
        logger.debug(s"Received type PrintSignal #$n : $self")
      case DivideNumbers(n, d) =>
        logger.debug(s"Received type DivideNumbers res = ${n / d} : $self")
      case BadStuff(ex) => throw new RuntimeException(ex)
    }

    override def preStart(): Unit = {
      super.preStart()
      logger.info(s"Child preStart for $self")
    }
    override def postStop(): Unit = {
      super.preStart()
      logger.info(s"Child postStop for $self")
    }
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      super.preRestart(reason, message)
      logger.info(s"Child preRestart with $message for $self")
    }

    override def postRestart(reason: Throwable): Unit = {
      super.postRestart(reason)
      logger.info(s"Child postRestart with $reason for $self")
    }
  }

}