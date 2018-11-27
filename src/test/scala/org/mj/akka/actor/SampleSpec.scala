package org.mj.akka.actor

import com.typesafe.scalalogging.LazyLogging
import org.junit.Assert.{assertEquals, assertNotEquals}
import org.junit.{After, AfterClass, Before, BeforeClass, Test}
import org.scalatest.junit.AssertionsForJUnit

object SampleSpec extends LazyLogging {
  org.apache.log4j.Logger.getRootLogger.setLevel(org.apache.log4j.Level.ALL)

  @BeforeClass
  def beforeClass(): Unit = {
    logger.debug("in beforeClass")
  }

  @AfterClass
  def afterClass(): Unit = {
    logger.debug("in afterClass")
  }

}

class SampleSpec extends AssertionsForJUnit with LazyLogging {
  @Test
  def testSuccess(): Unit = {
    assertNotEquals("message", "expected", "actual")
    assertEquals("message", "", "")
  }

  @Before
  def beforeTests(): Unit = {
    logger.info("in before")
  }

  @After
  def afterTests(): Unit = {
    logger.debug("in after")
  }

}
