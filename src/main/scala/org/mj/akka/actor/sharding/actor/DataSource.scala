package org.mj.akka.actor.sharding.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

// provides the sweet Engl style time lingo
import scala.concurrent.duration._


/**
  * Simulates a Kafka connection. spews out data for anyone asking for it
  *
  * Produces messages, do some preprocessing
  *
  * @param downStream is the actor that receives the result a sink reference
  *
  */
class DataSource(downStream: ActorRef, worker: ActorRef) extends Actor with ActorLogging {

  // need a ref to the system execution context
  implicit  val ec = context.dispatcher

  // used by 'ask' directive below
  implicit val to = Timeout(1 second)

  // populating this actor's mailbox (could be incorrectly done with some for loop)
  //
  // 'extends Actor' provides access to the Actor System
  // uses async properties of the scheduler
  context.system.scheduler.schedule(5 seconds, 1 second) {

    // create random string of len 5 / wrap in DataContainer
    // simulates, e.g., call Twitter api
    self ! DomainModel.DataContainer(scala.util.Random.nextString(scala.util.Random.nextInt(5)))
  }

  /**
    * accesses mailbox one at a time
    *
    * @return
    */
  override def receive: Receive = {

    // data handler for the DataContainer
    // ! = tell
    // ? = ask
    // ask the worker to do work '(worker ? dc)', then send result downstream 'pipeTo(downStream)', namely to a DataSink
    case dc: DomainModel.DataContainer => {
      (worker ? dc).pipeTo(downStream)

    }

  }



}
