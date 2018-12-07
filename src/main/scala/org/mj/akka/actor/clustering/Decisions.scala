package org.mj.akka.actor.clustering

import org.mj.akka.actor.clustering.Messages.{WorkGroup, WorkItem, WorkResult}

object Decisions {
  def whereShouldContainerGo(group: WorkGroup, item: WorkItem): WorkResult = {
    Thread.sleep(5) // just to simulate resource hunger
    val seed = util.Random.nextInt(10000)
    WorkResult(s"CVR_${group.id}_$seed")
  }
}
