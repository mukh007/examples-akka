package org.mj.akka.actor.example

import akka.actor._
import akka.routing.{ActorRefRoutee, FromConfig, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

object ActorRoutingApp extends App with LazyLogging {
  val sys = ActorSystem("ActorRoutingApp", ConfigFactory.load("ActorRoutingApp.conf").withFallback(ConfigFactory.load()))
  val master = sys.actorOf(Props[Master], "master")
  for (i <- 0 to 9) { // 0->a, 1->b, 2->c, 3->d, 4->e, 5->a, 6->b, ...
    master ! Work(s"Master Naive Work $i")
  }

  val router1: ActorRef = sys.actorOf(FromConfig.props(Props[Worker]), "router1")
  for (i <- 0 to 9) {
    router1 ! Work(s"Router1 Naive Work $i")
  }

  Thread sleep 1000
  sys.terminate()
}

class Master extends Actor with LazyLogging {
  private var router = {
    val routes = Vector.fill(5) {
      val r = context.actorOf(Props[Worker])
      context watch r
      ActorRefRoutee(r)
    }
    // The Router is immutable and the RoutingLogic is thread safe; meaning that they can also be used outside of actors.
    Router(RoundRobinRoutingLogic(), routes)
  }

  def receive: PartialFunction[Any, Unit] = {
    case w: Work =>
      router.route(w, sender())
    case Terminated(a) =>
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[Worker])
      context watch r
      router = router.addRoutee(r)
  }
}


class Worker extends Actor with LazyLogging {
  def receive: PartialFunction[Any, Unit] = {
    case w: Work =>
      logger.info(s"Job '${w.job}' received at ${self.path}")
  }
}

case class Work(job: String)