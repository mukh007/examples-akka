package org.mj.akka.actor.iot

import akka.actor.{Actor, ActorLogging, Props}
import akka.japi.Option.Some

object Device {
  def props(groupId: String, deviceId: String): Props = Props(new Device(groupId, deviceId))

  case class ReadTemperature(requestId: Long)

  case class RecordTemperature(requestId: Long, value: Double)

  case class TemperatureRecorded(requestId: Long)

  case class RespondTemperature(requestId: Long, value: Option[Double])

}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {

  import Device._

  private var lastTemperatureReading: Option[Double] = None

  override def preStart(): Unit = log.debug("Device actor {}-{} started", groupId, deviceId)

  override def postStop(): Unit = log.debug("Device actor {}-{} stopped", groupId, deviceId)

  override def receive: Receive = {
    case in: ReadTemperature =>
      val currSender = sender()
      currSender ! RespondTemperature(in.requestId, lastTemperatureReading)
    case in: RecordTemperature =>
      val currSender = sender()
      log.debug(s"RecordTemperature $in")
      lastTemperatureReading = Some(in.value)
      currSender ! TemperatureRecorded(in.requestId)
    case DeviceManager.RequestTrackDevice(`groupId`, `deviceId`) =>
      val currSender = sender()
      log.info("Registering for {}-{} as this actor is responsible for {}-{}.", groupId, deviceId, this.groupId, this.deviceId)
      currSender ! DeviceManager.DeviceRegistered
    case in: DeviceManager.RequestTrackDevice =>
      log.warning("Ignoring for {}-{} as this actor is responsible for {}-{}.", in.groupId, in.deviceId, this.groupId, this.deviceId)
  }
}
