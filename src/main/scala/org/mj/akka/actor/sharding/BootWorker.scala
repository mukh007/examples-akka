package org.mj.akka.actor.sharding

import akka.actor.ActorSystem
import akka.cluster.seed.ZookeeperClusterSeed
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import org.mj.akka.actor.sharding.actor.Worker

/**
  * Will run a Worker shard.
  *
  */
object BootWorker extends App {

  // need an ActorSystem
  val system = ActorSystem("MyActorSystem")

  //System joins the cluster using a random port.
  ZookeeperClusterSeed(system).join()


  // same code that's in AppRunner.scala
  //
  //bring up the  shard for this node
  // region is an actor
  val region = ClusterSharding(system).start(
    typeName = Worker.shardName,
    entityProps = Worker.props(), // where we pick the type of actor to be booted on this JVM
    settings = ClusterShardingSettings(system),
    extractEntityId = Worker.idExtractor,
    extractShardId = Worker.shardResolver
  )

}
