package org.mj.akka.actor.iot

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.junit.Assert.{assertEquals, assertNotEquals}
import org.junit.{After, AfterClass, Before, BeforeClass, Test}
import org.scalatest.junit.AssertionsForJUnit

import scala.concurrent.duration._

object DeviceTest extends LazyLogging {
  @BeforeClass
  def beforeClass(): Unit = {
    logger.trace("in beforeClass")
  }

  @AfterClass
  def afterClass(): Unit = {
    logger.trace("in afterClass")
  }

}

class DeviceTest extends AssertionsForJUnit with LazyLogging {
  private implicit val as: ActorSystem = ActorSystem("DeviceSpec")
  private implicit val to: Timeout = 1.second

  @Test
  def testDeviceActor(): Unit = {
    val probe = TestProbe()
    val deviceActor = as.actorOf(Device.props("group", "device"))

    deviceActor.tell(Device.RecordTemperature(1, 24.0), probe.ref)
    val response1 = probe.expectMsg(Device.TemperatureRecorded(1))
    assertEquals("Invalid response1 requestId", response1.requestId, 1)

    deviceActor.tell(Device.ReadTemperature(requestId = 2), probe.ref)
    val response2 = probe.expectMsgType[Device.RespondTemperature]
    assertEquals("Invalid response2 requestId", response2.requestId, 2)
    assertNotEquals("Invalid response2 value", response2.value, None)
    assertEquals("Invalid response2 value", response2.value, Some(24))

    deviceActor.tell(Device.RecordTemperature(requestId = 3, 55.0), probe.ref)
    val response3 = probe.expectMsg(Device.TemperatureRecorded(3))
    assertEquals("Invalid response3 requestId", response3.requestId, 3)

    deviceActor.tell(Device.ReadTemperature(requestId = 4), probe.ref)
    val response4 = probe.expectMsgType[Device.RespondTemperature]
    assertEquals("Invalid response4 requestId", response4.requestId, 4)
    assertEquals("Invalid response4 value", response4.value, Some(55.0))
  }

  @Test
  def testDeviceRegistrationSuccessful(): Unit = {
    val probe = TestProbe()
    val deviceActor = as.actorOf(Device.props("group", "device"))

    deviceActor.tell(DeviceManager.RequestTrackDevice("group", "device"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    assertEquals(probe.lastSender, deviceActor)
  }

  @Test
  def testDeviceRegistrationFailed(): Unit = {
    val probe = TestProbe()
    val deviceActor = as.actorOf(Device.props("group", "device"))

    deviceActor.tell(DeviceManager.RequestTrackDevice("invalidGroup", "device"), probe.ref)
    probe.expectNoMsg(500.millisecond)
    deviceActor.tell(DeviceManager.RequestTrackDevice("group", "invalidDevice"), probe.ref)
    probe.expectNoMsg(500.millisecond)
  }

  @Test
  def testDeviceFoo(): Unit = {}

  @Before
  def beforeTests(): Unit = {
    logger.trace("in before")
  }

  @After
  def afterTests(): Unit = {
    logger.trace("in after")
  }

}
