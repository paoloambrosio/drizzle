package net.paoloambrosio.drizzle.runner

import akka.actor.{Actor, Props}
import net.paoloambrosio.drizzle.metrics.{TimedActionMetrics, MetricsRepository}

object MetricsCollector {

  def props(repositories: Seq[MetricsRepository]) = Props(new MetricsCollector(repositories))
}

/**
  * Simple actor that writes run metrics to multiple repositories.
  *
  * @param repositories
  */
class MetricsCollector(repositories: Seq[MetricsRepository]) extends Actor {

  // TODO How are we going to handle async writes?
  override def receive: Receive = {
    case t: TimedActionMetrics => repositories.foreach(_.store(t))
  }

}
