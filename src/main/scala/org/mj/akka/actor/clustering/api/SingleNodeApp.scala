package org.mj.akka.actor.clustering.api

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.mj.akka.actor.clustering.DecidersGuardian

object SingleNodeApp extends App {
  val config = ConfigFactory.load()
  implicit val system = ActorSystem(config getString "application.name")

  val decider = system.actorOf(Props[DecidersGuardian])
  system.actorOf(Props(classOf[RestInterface], decider, 8080))
}
