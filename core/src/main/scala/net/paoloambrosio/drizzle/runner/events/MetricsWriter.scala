package net.paoloambrosio.drizzle.runner.events

import akka.actor.{Actor, Props}
import net.paoloambrosio.drizzle.metrics.{MetricsRepository, RuntimeInfo, TimedActionMetrics}

object MetricsWriter {

  def props(repository: MetricsRepository) = Props(new MetricsWriter(repository))
}

/**
  * Simple actor that writes run metrics to a single repository.
  *
  * @param repository
  */
class MetricsWriter(repository: MetricsRepository) extends Actor {

  // TODO How are we going to handle async writes?
  override def receive: Receive = {
    case m: VUserMetrics => repository.store(TimedActionMetrics(
      simulation = RuntimeInfo(None,"sim-id"), vuser = RuntimeInfo(None,"vus-id"), action = RuntimeInfo(None,"act-id"),
      start = m.start, elapsedTime = m.elapsedTime
    ))
  }

}
