package org.mj.akka.actor.example

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object AskPatternApp extends App with LazyLogging {
  private val as = ActorSystem("AskPatternApp")
  private implicit val ec: ExecutionContext = as.dispatcher
  private implicit val timeout: Timeout = Timeout(10.seconds)

  private val actor1 = as.actorOf(Props(new AskActor("actor1")), "AskActor1")
  private val actor2 = as.actorOf(Props(new AskActor("actor2")), "AskActor2")
  private val actor3 = as.actorOf(Props(new AskActor("actor3")), "AskActor3")

  logger.info("Starting...")

  val ans1 = actor1 ? AskName
  val ans2 = actor2 ? AskName
  val ans3 = actor2 ? AskNameOf(actor3)

  Future.sequence(Seq(ans1, ans2)).foreach(name => logger.info(s"Name is $name"))

  logger.info("Finished...")

  as.terminate()
}

case object AskName

case class AskNameOf(name: ActorRef)

case class NameResponse(name: String)

class AskActor(name: String)(implicit to: Timeout, ec: ExecutionContext) extends Actor with LazyLogging {
  override def receive: Receive = {
    case AskName =>
      logger.debug(s"Received : $name by $self")
      sender ? NameResponse(name)
    case AskNameOf(other) =>
      logger.debug(s"Received : $name by $self")
      val resF = other ? AskName
      resF.onComplete {
        case Success(nr: NameResponse) => logger.info(s"They said name was $nr")
        case Success(value) => logger.info(s"They did not tell their name $value")
        case Failure(ex) => logger.error(s"They failed the name check failed $ex")
      }
      val currentSender = sender
      Future(currentSender ! s"Done $resF!")
    case any: Any =>
      logger.debug(s"Received : $name ($any) by $self")
  }
}