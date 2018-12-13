package org.mj.akka.actor.example

import akka.actor._
import akka.cluster.seed.ZookeeperClusterSeed
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.routing.FromConfig
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object ActorRoutingPoolClusterApp extends App with LazyLogging {
  private val config = ConfigFactory.load("ActorRoutingPoolClusterApp")
  private implicit val system: ActorSystem = ActorSystem(config getString "application.name", config)
  private implicit val ec: ExecutionContext = system.dispatcher
  private implicit val to: Timeout = 5.seconds

  ZookeeperClusterSeed(system).join()
  ClusterSharding(system).start(
    typeName = Master.name,
    //entityProps = Master.props,
    entityProps = Props(new Master(workerRouter)),
    settings = ClusterShardingSettings(system),
    extractShardId = Master.extractShardId,
    extractEntityId = Master.extractEntityId
  )
  val master: ActorRef = ClusterSharding(system).shardRegion(Master.name)
  val workerRouter: ActorRef = system.actorOf(FromConfig.props(Props[Worker]), "router1")
  //val master: ActorRef = system.actorOf(Props[Master], "master")

  Thread.sleep(10000)
  for (i <- 0 to 10) {
    master ! WorkRequest(i, s"WorkItem $i")
  }

  //  Thread sleep 2000
  //  system.terminate()

  // Internal classes
  object Master {
    def name = "Master"

    //def props: Props = Props[Master]

    // Sharding logic goes here
    def extractShardId: ExtractShardId = {
      case WorkRequest(id, _) => (id % 2).toString
    }

    // Routing logic goes here
    def extractEntityId: ExtractEntityId = {
      case msg@WorkRequest(id, jobId) => (s"$id-$jobId", msg)
    }
  }

  class Master(workerRouter: ActorRef) extends Actor with LazyLogging {
    // biz logic goes here to deal with $wd
    def receive: Receive = {
      case wd: WorkDone =>
        logger.trace(s"$wd by ${self.path.name}")
      case wr: WorkRequest =>
        workerRouter ! wr
    }
  }

  class Worker extends Actor with LazyLogging {
    def receive: Receive = {
      case wr: WorkRequest =>
        val curSender = sender()
        Future {
          Thread.sleep(Random.nextInt(50))
          logger.info(s"MJJob '${wr.job}' received at ${self.path.parent.name}-${self.path.name}")
          curSender ! WorkDone(wr.job, s"Done@${System.nanoTime}_by_${self.path.name}")
        }
    }
  }

  case class WorkRequest(id: Int, job: String)

  case class WorkDone(job: String, result: String)

}

