package org.mj.akka.actor.clustering

import akka.actor.{Actor, Props}
import Messages._

class DecidersGuardian extends Actor {
  override def receive: Receive = {
    case m: WhereShouldIGo =>
      val name = s"J${m.group.id}"
      val actor = context.child(name) getOrElse context.actorOf(Props[ShardingDecider], name)
      actor forward m
  }
}
