package org.mj.akka.actor.clustering

import akka.actor.{Actor, Props}
import Messages._

class DecidersGuardian extends Actor {
  override def receive: Receive = {
    case m: DoSomeWork =>
      val name = s"J${m.groupId.id}"
      val actor = context.child(name) getOrElse context.actorOf(Props[ShardingDecider], name)
      actor forward m
  }
}
