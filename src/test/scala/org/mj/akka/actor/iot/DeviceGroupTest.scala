package org.mj.akka.actor.iot

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.junit.Assert.{assertEquals, assertNotEquals}
import org.junit.{After, AfterClass, Before, BeforeClass, Test}
import org.scalatest.junit.AssertionsForJUnit

import scala.concurrent.duration._

object DeviceGroupTest extends LazyLogging {
  @BeforeClass
  def beforeClass(): Unit = {
    logger.trace("in beforeClass")
  }

  @AfterClass
  def afterClass(): Unit = {
    logger.trace("in afterClass")
  }

}

class DeviceGroupTest extends AssertionsForJUnit with LazyLogging {
  private implicit val as: ActorSystem = ActorSystem("DeviceSpec")
  private implicit val to: Timeout = 10.second

  @Test
  def testDeviceGroupRegistrationSuccess(): Unit = {
    val probe = TestProbe()
    val groupActor = as.actorOf(DeviceGroup.props("group"))

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor1 = probe.lastSender

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor2 = probe.lastSender
    assertNotEquals(probe.lastSender, groupActor)

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor3 = probe.lastSender

    assertNotEquals("Different devices have different actors", deviceActor1, deviceActor2)
    assertEquals("Same devices use same actor", deviceActor1, deviceActor3)

    // Check that the device actors are working
    deviceActor1.tell(Device.RecordTemperature(requestId = 0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 0))
    deviceActor2.tell(Device.RecordTemperature(requestId = 1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
  }

  @Test
  def testDeviceGroupRegistrationFailed(): Unit = {
    val probe = TestProbe()
    val groupActor = as.actorOf(DeviceGroup.props("group"))

    groupActor.tell(DeviceManager.RequestTrackDevice("invalidGroup", "device"), probe.ref)
    probe.expectNoMsg(500.millisecond)
  }

  @Test
  def testGetListOfActiveDevices(): Unit = {
    val probe = TestProbe()
    val groupActor = as.actorOf(DeviceGroup.props("group"))

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device1Actor = probe.lastSender
    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    groupActor.tell(DeviceGroup.RequestDeviceList(requestId = 0), probe.ref)
    probe.expectMsg(DeviceGroup.ReplyDeviceList(requestId = 0, Set("device1", "device2")))

    probe.watch(device1Actor)
    device1Actor ! PoisonPill
    // using awaitAssert to retry because it might take longer for the groupActor
    // to see the Terminated, that order is undefined
    probe.expectTerminated(device1Actor)
    probe.awaitAssert {
      groupActor.tell(DeviceGroup.RequestDeviceList(requestId = 1), probe.ref)
      probe.expectMsg(DeviceGroup.ReplyDeviceList(requestId = 1, Set("device2")))
    }
  }

  @Test
  def testQueryAllDevicesFromGroup(): Unit = {
    val probe = TestProbe()
    val groupActor = as.actorOf(DeviceGroup.props("group"))

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor1 = probe.lastSender

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor2 = probe.lastSender

    groupActor.tell(DeviceManager.RequestTrackDevice("group", "device3"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val deviceActor3 = probe.lastSender

    // Check that the device actors are working
    deviceActor1.tell(Device.RecordTemperature(requestId = 0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 0))
    deviceActor2.tell(Device.RecordTemperature(requestId = 1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
    // No temperature for device3

    groupActor.tell(DeviceGroup.RequestAllTemperatures(requestId = 0), probe.ref)
    probe.expectMsg(
      DeviceGroup.RespondAllTemperatures(
        requestId = 0,
        temperatures = Map(
          "device1" -> DeviceGroup.Temperature(1.0),
          "device2" -> DeviceGroup.Temperature(2.0),
          "device3" -> DeviceGroup.TemperatureNotAvailable)))
  }

  @Before
  def beforeTests(): Unit = {
    logger.trace("in before")
  }

  @After
  def afterTests(): Unit = {
    logger.trace("in after")
  }

}
