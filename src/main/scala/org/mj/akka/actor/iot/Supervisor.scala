package org.mj.akka.actor.iot

import akka.actor.{Actor, ActorLogging, Props}

object Supervisor {
  def id = "IotSupervisor"

  def props(): Props = Props(new Supervisor)
}

class Supervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("IoT Application started")

  override def postStop(): Unit = log.info("IoT Application stopped")

  override def receive: Receive = Actor.emptyBehavior
}
