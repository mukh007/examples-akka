package org.mj.akka.actor.clustering.api

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.seed.ZookeeperClusterSeed
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory
import org.mj.akka.actor.clustering.{ShardingDecider, WorkRouter}

object ShardedApp extends App {
  val config = ConfigFactory.load("sharded")
  private implicit val system: ActorSystem = ActorSystem(config getString "application.name", config)

  ZookeeperClusterSeed(system).join()

  private val workRouter: ActorRef = system.actorOf(FromConfig.props(Props[WorkRouter]), "workRouter")

  ClusterSharding(system).start(
    typeName = ShardingDecider.name,
    //entityProps = ShardingDecider.props,
    entityProps = Props(new ShardingDecider(workRouter)),
    settings = ClusterShardingSettings(system),
    extractShardId = ShardingDecider.extractShardId,
    extractEntityId = ShardingDecider.extractEntityId
  )

  private val decider = ClusterSharding(system).shardRegion(ShardingDecider.name)

  system.actorOf(Props(new RestInterface(decider, config getInt "application.portId")))
}
