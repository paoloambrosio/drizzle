package net.paoloambrosio.drizzle.runner

import akka.actor.{Actor, Props}
import net.paoloambrosio.drizzle.metrics.{SimulationMetrics, MetricsRepository}

object MetricsCollector {

  def props(repositories: Seq[MetricsRepository])(simulationMetrics: SimulationMetrics): Props =
    Props(new MetricsCollector(repositories, simulationMetrics))
}

/**
  * Simple actor that writes run metrics to multiple repositories.
  *
  * @param repositories
  */
class MetricsCollector(repositories: Seq[MetricsRepository], simulationMetrics: SimulationMetrics) extends Actor {

  override def receive: Receive = ??? // HOW ARE WE GOING TO HANDLE ASYNC WRITES?

}
