package org.mj.akka.actor.model

import akka.actor.{Actor, ActorLogging, Props}

object ModelHandler {
  def props(groupId: String, deviceId: String): Props = Props(new ModelHandler(groupId, deviceId))

  case class ModelLoad(requestId: String)

  case class ModelLoaded(requestId: String)

  case class ModelApply(requestId: String)

  case class ModelResponse(requestId: String, value: Option[String])

}

class ModelHandler(groupId: String, deviceId: String) extends Actor with ActorLogging {

  import ModelHandler._

  override def receive: Receive = {
    case in: ModelLoad =>
      val currSender = sender()
      log.debug(s"Load Model $in")
      currSender ! ModelLoaded(in.requestId)
    case in: ModelApply =>
      val currSender = sender()
      log.debug(s"ApplyModel $in")
      currSender ! ModelResponse(in.requestId, Some(s"${in.requestId}-${applyModel()}"))
  }

  private def applyModel() = "ModelApplied"
}
