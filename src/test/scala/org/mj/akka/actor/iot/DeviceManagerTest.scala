package org.mj.akka.actor.iot

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.junit.Assert.{assertEquals, assertNotEquals}
import org.junit.{After, AfterClass, Before, BeforeClass, Test}
import org.scalatest.junit.AssertionsForJUnit

import scala.concurrent.duration._

object DeviceManagerTest extends LazyLogging {
  @BeforeClass
  def beforeClass(): Unit = {
    logger.trace("in beforeClass")
  }

  @AfterClass
  def afterClass(): Unit = {
    logger.trace("in afterClass")
  }

}

class DeviceManagerTest extends AssertionsForJUnit with LazyLogging {
  private implicit val as: ActorSystem = ActorSystem("DeviceSpec")
  private implicit val to: Timeout = 10.second

  @Test
  def testGetListOfActiveDevices(): Unit = {
    val probe = TestProbe()
    val managerActor = as.actorOf(DeviceManager.props())

    managerActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device1Actor = probe.lastSender
    managerActor.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device2Actor = probe.lastSender
    managerActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device3Actor = probe.lastSender

    assertNotEquals(device1Actor, device2Actor)
    assertEquals(device1Actor, device3Actor)

    // Check that the device actors are working
    device1Actor.tell(Device.RecordTemperature(requestId = 0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 0))
    device2Actor.tell(Device.RecordTemperature(requestId = 1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))

    managerActor.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    probe.watch(device2Actor)
    device2Actor ! PoisonPill
    probe.expectTerminated(device2Actor)
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
