package org.mj.akka.actor.clustering.api

import akka.actor.{ActorSystem, Props}
import akka.cluster.seed.ZookeeperClusterSeed
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.typesafe.config.ConfigFactory
import org.mj.akka.actor.clustering.ShardingDecider

object ShardedApp extends App {
  val config = ConfigFactory.load("sharded")
  private implicit val system = ActorSystem(config getString "application.name", config)

  ZookeeperClusterSeed(system).join()

  ClusterSharding(system).start(
    typeName = ShardingDecider.name,
    entityProps = ShardingDecider.props,
    settings = ClusterShardingSettings(system),
    extractShardId = ShardingDecider.extractShardId,
    extractEntityId = ShardingDecider.extractEntityId
  )

  val decider = ClusterSharding(system).shardRegion(ShardingDecider.name)
  system.actorOf(Props(new RestInterface(decider, config getInt "application.exposed-port")))
}
