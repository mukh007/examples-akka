package org.mj.akka.actor.sharding.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe

import scala.concurrent.Future

/**
  * Campanion object that defines the sharding properties for the Worker shard.
  *
  * Doesn't have to be done in a companion object. could come from a config.
  *
  *
  *
  */
object Worker {

  val shardName: String = "WorkerShard"

  def props() =
    Props(new Worker())

  val idExtractor: ShardRegion.ExtractEntityId = {
    case p: DomainModel.DataContainer => (p.data, p)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case DomainModel.DataContainer(data) => {

      // determines which shard region gets the work as well as the entity that will do the work
      // using the hash of the string to determine this
      (math.abs(data.hashCode) % 100).toString
    }
  }
}


/**
  * This is the service that will do some processing on the strings
  *
  * a pool of these guys
  *
  */
class Worker extends Actor with ActorLogging {
  private implicit val ec = context.dispatcher

  def work(s: DomainModel.DataContainer): Future[DomainModel.DataContainer] = Future {
    Thread.sleep(500)
    println("***********WORKING************* " + self.path)
    DomainModel.DataContainer(s.data.reverse + s.data.length)
  }


  override def receive: Receive = {
    // data handler that processes the string
    case dc: DomainModel.DataContainer => {

      // sender is a method on Actor that returns the sender of the oldest msg in the mailbox
      val s = sender()

      // in the future, send result of work back to sender
      work(dc).pipeTo(s)

    }

  }


}
