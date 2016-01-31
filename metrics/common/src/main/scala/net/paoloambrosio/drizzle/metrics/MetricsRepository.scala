package net.paoloambrosio.drizzle.metrics

import scala.concurrent.Future

trait MetricsRepository {

  /**
    * Store metrics for a single request into the repository.
    *
    * @param request Request metrics to store
    * @return
    */
  def store(request: TimedActionMetrics): Future[Unit]
}
