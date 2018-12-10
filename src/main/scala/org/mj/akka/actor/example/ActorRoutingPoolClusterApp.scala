package org.mj.akka.actor.example

import akka.actor._
import akka.cluster.seed.ZookeeperClusterSeed
import akka.routing.FromConfig
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object ActorRoutingPoolClusterApp extends App with LazyLogging {
  val config = ConfigFactory.load("ActorRoutingPoolClusterApp")
  private implicit val system: ActorSystem = ActorSystem(config getString "application.name", config)
  //  val system = ActorSystem("ActorRoutingPoolClusterApp", ConfigFactory.load("ActorRoutingPoolClusterApp.conf").withFallback(ConfigFactory.load()))

  ZookeeperClusterSeed(system).join()

  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val to: Timeout = 5.seconds

  val master: ActorRef = system.actorOf(Props[Master], "master")

  Thread.sleep(10000)
  for (i <- 0 to 50) {
    val res = master ! WorkRequest(s"WorkItem $i")
  }

  //  Thread sleep 2000
  //  system.terminate()

  // Internal classes
  class Master extends Actor with LazyLogging {
    val router: ActorRef = system.actorOf(FromConfig.props(Props[Worker]), "router1")

    // biz logic goes here to deal with $wd
    def receive: Receive = {
      case wd: WorkDone =>
        logger.info(s"$wd by ${self.path.name}")
      case wr: WorkRequest =>
        router ! wr
    }
  }

  class Worker extends Actor with LazyLogging {
    def receive: Receive = {
      case wr: WorkRequest =>
        val curSender = sender()
        Future {
          Thread.sleep(Random.nextInt(50))
          logger.info(s"MJJob '${wr.job}' received at ${self.path}")
          curSender ! WorkDone(wr.job, s"Done@${System.nanoTime}_by_${self.path.name}")
        }
    }
  }

  case class WorkRequest(job: String)

  case class WorkDone(job: String, result: String)

}

