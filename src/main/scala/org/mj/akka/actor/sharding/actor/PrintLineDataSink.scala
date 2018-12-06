package org.mj.akka.actor.sharding.actor

import akka.actor.{Actor, ActorLogging}

class PrintLineDataSink extends Actor with ActorLogging {

  /**
    * Prints data to stdout
    *
    * @return
    */
  override def receive: Receive = {

    case DomainModel.DataContainer(data) => println(data)

  }

}

