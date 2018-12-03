package org.mj.akka.actor.model

import akka.actor.{Actor, ActorLogging, Props}

object Supervisor {
  def id = "ModelServiceSupervisor"

  def props(): Props = Props(new Supervisor)
}

class Supervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("ModelService App started")

  override def postStop(): Unit = log.info("ModelService App stopped")

  override def receive: Receive = Actor.emptyBehavior
}
