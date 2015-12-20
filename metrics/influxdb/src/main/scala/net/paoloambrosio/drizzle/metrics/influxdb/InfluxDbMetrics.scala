package net.paoloambrosio.drizzle.metrics.influxdb

import java.time.{ZoneOffset, OffsetDateTime}

import com.paulgoldbaum.influxdbclient.{Point, Database}
import net.paoloambrosio.drizzle.metrics.{Request, MetricsRepository}

import scala.concurrent.{ExecutionContext, Future}

class InfluxDbMetrics(metricsDb: Database)(implicit ec: ExecutionContext) extends MetricsRepository {

  val S_IN_NS = 1000000000L
  val MCS_IN_NS = 1000

  override def store(request: Request): Future[Unit] = {
    val requestDurationMcs = request.responseTime.getNano / MCS_IN_NS
    val point = Point(key = "request", timestamp = toNanoTimestamp(request.absoluteStart))
      .addTag("runId", request.vuser.run.id)
      .addTag("vUserId", request.vuser.id)
      .addTag("requestId", request.id)
      .addField("responseTime", requestDurationMcs)
    metricsDb.write(point) map { _ => () }
  }

  def toNanoTimestamp(dateTime: OffsetDateTime) = {
    val utc = dateTime.atZoneSameInstant(ZoneOffset.UTC)
    utc.toEpochSecond * S_IN_NS + utc.getNano.toLong
  }
}
