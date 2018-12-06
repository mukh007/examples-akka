package org.mj.akka.actor.sharding

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.seed.ZookeeperClusterSeed
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import org.mj.akka.actor.sharding.actor.{DataSource, PrintLineDataSink, Worker}

/**
  * Kicks off the entire app; analogous to the BYOM BootTrainerService
  *
  */
object AppRunner extends App {

  // need an ActorSystem
  val system = ActorSystem("MyActorSystem")

  //System joins the cluster using a random port.
  ZookeeperClusterSeed(system).join()


  // props -- recipe for creating the actor
  //
  //
  val sinkRef: ActorRef = system.actorOf(Props(new PrintLineDataSink))


  //bring up the  shard for this node
  // region is an actor
  val region = ClusterSharding(system).start(
    typeName = Worker.shardName,
    entityProps = Worker.props(), // where we pick the type of actor to be booted on this JVM
    settings = ClusterShardingSettings(system),
    extractEntityId = Worker.idExtractor,
    extractShardId = Worker.shardResolver
  )

  // region replaces the simple non-cluster version of the workerRef here
  // val workerRef: ActorRef = system.actorOf(Props(new Worker))

  val sourceRef: ActorRef = system.actorOf(Props(new DataSource(sinkRef, region)))


}