package net.paoloambrosio.drizzle.metrics.influxdb

import java.time.{ZoneOffset, OffsetDateTime}

import com.paulgoldbaum.influxdbclient.{Point, Database}
import net.paoloambrosio.drizzle.metrics.{TimedActionMetrics, MetricsRepository}

import scala.concurrent.{ExecutionContext, Future}

class InfluxDbMetrics(metricsDb: Database)(implicit ec: ExecutionContext) extends MetricsRepository {

  val S_IN_NS = 1000000000L
  val MCS_IN_NS = 1000

  override def store(metrics: TimedActionMetrics): Future[Unit] = {
    val requestDurationMcs = metrics.elapsedTime.getNano / MCS_IN_NS
    val point = Point(key = "action", timestamp = toNanoTimestamp(metrics.absoluteStart))
      .addTag("runId", metrics.vuser.run.id)
      .addTag("vUserId", metrics.vuser.id)
      .addTag("requestId", metrics.id)
      .addField("elapsedTime", requestDurationMcs)
    metricsDb.write(point) map { _ => () }
  }

  def toNanoTimestamp(dateTime: OffsetDateTime) = {
    val utc = dateTime.atZoneSameInstant(ZoneOffset.UTC)
    utc.toEpochSecond * S_IN_NS + utc.getNano.toLong
  }
}
