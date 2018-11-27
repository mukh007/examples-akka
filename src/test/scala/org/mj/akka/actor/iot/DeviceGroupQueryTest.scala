package org.mj.akka.actor.iot

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.junit.{AfterClass, BeforeClass, Test}
import org.scalatest.junit.AssertionsForJUnit

import scala.concurrent.duration._

object DeviceGroupQueryTest extends LazyLogging {
  @BeforeClass
  def beforeClass(): Unit = {
    logger.trace("in beforeClass")
  }

  @AfterClass
  def afterClass(): Unit = {
    logger.trace("in afterClass")
  }

}

class DeviceGroupQueryTest extends AssertionsForJUnit with LazyLogging {
  private implicit val as: ActorSystem = ActorSystem(s"${this.getClass.getSimpleName}")
  private implicit val to: Timeout = 10.second

  @Test
  def testReturnsValueForAllDevices(): Unit = {
    val requester = TestProbe()
    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = as.actorOf(DeviceGroupQuery.props(
      actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2"),
      requestId = 1,
      requester = requester.ref,
      timeout = 3.seconds))

    device1.expectMsg(Device.ReadTemperature(requestId = 0))
    device2.expectMsg(Device.ReadTemperature(requestId = 0))

    queryActor.tell(Device.RespondTemperature(requestId = 0, Some(1.0)), device1.ref)
    queryActor.tell(Device.RespondTemperature(requestId = 0, Some(2.0)), device2.ref)

    requester.expectMsg(DeviceGroup.RespondAllTemperatures(
      requestId = 1,
      temperatures = Map(
        "device1" -> DeviceGroup.Temperature(1.0),
        "device2" -> DeviceGroup.Temperature(2.0))))
  }

  @Test
  def testTemperatureNotAvailable(): Unit = {
    val requester = TestProbe()

    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = as.actorOf(DeviceGroupQuery.props(
      actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2"),
      requestId = 1,
      requester = requester.ref,
      timeout = 3.seconds))

    device1.expectMsg(Device.ReadTemperature(requestId = 0))
    device2.expectMsg(Device.ReadTemperature(requestId = 0))

    queryActor.tell(Device.RespondTemperature(requestId = 0, None), device1.ref)
    queryActor.tell(Device.RespondTemperature(requestId = 0, Some(2.0)), device2.ref)

    requester.expectMsg(DeviceGroup.RespondAllTemperatures(
      requestId = 1,
      temperatures = Map(
        "device1" -> DeviceGroup.TemperatureNotAvailable,
        "device2" -> DeviceGroup.Temperature(2.0))))
  }

  @Test
  def testDeviceNotAvailable(): Unit = {
    val requester = TestProbe()

    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = as.actorOf(DeviceGroupQuery.props(
      actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2"),
      requestId = 1,
      requester = requester.ref,
      timeout = 3.seconds))

    device1.expectMsg(Device.ReadTemperature(requestId = 0))
    device2.expectMsg(Device.ReadTemperature(requestId = 0))

    queryActor.tell(Device.RespondTemperature(requestId = 0, Some(1.0)), device1.ref)
    device2.ref ! PoisonPill

    requester.expectMsg(DeviceGroup.RespondAllTemperatures(
      requestId = 1,
      temperatures = Map(
        "device1" -> DeviceGroup.Temperature(1.0),
        "device2" -> DeviceGroup.DeviceNotAvailable)))
  }

  @Test
  def testDeviceStoppedAnswering(): Unit = {
    val requester = TestProbe()

    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = as.actorOf(DeviceGroupQuery.props(
      actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2"),
      requestId = 1,
      requester = requester.ref,
      timeout = 3.seconds))

    device1.expectMsg(Device.ReadTemperature(requestId = 0))
    device2.expectMsg(Device.ReadTemperature(requestId = 0))

    queryActor.tell(Device.RespondTemperature(requestId = 0, Some(1.0)), device1.ref)
    queryActor.tell(Device.RespondTemperature(requestId = 0, Some(2.0)), device2.ref)
    device2.ref ! PoisonPill

    requester.expectMsg(DeviceGroup.RespondAllTemperatures(
      requestId = 1,
      temperatures = Map(
        "device1" -> DeviceGroup.Temperature(1.0),
        "device2" -> DeviceGroup.Temperature(2.0))))
  }

  @Test
  def testDeviceTimedOut(): Unit = {
    val requester = TestProbe()

    val device1 = TestProbe()
    val device2 = TestProbe()

    val queryActor = as.actorOf(DeviceGroupQuery.props(
      actorToDeviceId = Map(device1.ref -> "device1", device2.ref -> "device2"),
      requestId = 1,
      requester = requester.ref,
      timeout = 1.second))

    device1.expectMsg(Device.ReadTemperature(requestId = 0))
    device2.expectMsg(Device.ReadTemperature(requestId = 0))

    queryActor.tell(Device.RespondTemperature(requestId = 0, Some(1.0)), device1.ref)

    requester.expectMsg(DeviceGroup.RespondAllTemperatures(
      requestId = 1,
      temperatures = Map(
        "device1" -> DeviceGroup.Temperature(1.0),
        "device2" -> DeviceGroup.DeviceTimedOut)))
  }
}
