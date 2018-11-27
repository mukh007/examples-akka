package org.mj.akka.actor.iot

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

/**
  * https://doc.akka.io/docs/akka/2.5.5/scala/guide/tutorial_2.html
  */
object App extends App with LazyLogging {
  private val as = ActorSystem("IOTApp")
  val supervisor = as.actorOf(Supervisor.props(), Supervisor.id)

  Thread.sleep(1000)
  logger.info(s"Terminating...")
  as.terminate
}
