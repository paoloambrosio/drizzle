package net.paoloambrosio.drizzle.metrics.influxdb

import java.time.{ZoneOffset, OffsetDateTime}

import com.paulgoldbaum.influxdbclient.{Point, Database}
import net.paoloambrosio.drizzle.metrics.{TimedActionMetrics, MetricsRepository}

import scala.concurrent.{ExecutionContext, Future}

object InfluxDbMetrics {

  val ActionType = "action"
  val ActionIdField = "id"
  val ActionNameField = "name"
  val VUserIdField = "vUserId"
  val VUserNameField = "vUserName"
  val SimulationIdField = "simulationId"
  val SimulationNameField = "simulationName"
  val ElapsedTimeField = "elapsedTime"
}

class InfluxDbMetrics(metricsDb: Database)(implicit ec: ExecutionContext) extends MetricsRepository {

  import InfluxDbMetrics._

  val S_IN_NS = 1000000000L
  val MCS_IN_NS = 1000

  override def store(metrics: TimedActionMetrics): Future[Unit] = {
    val requestDurationMcs = metrics.elapsedTime.getNano / MCS_IN_NS
    val point = Point(key = ActionType, timestamp = toNanoTimestamp(metrics.start))
      .addTag(ActionIdField, metrics.action.id)
      .addTag(ActionNameField, metrics.action.name)
      .addTag(VUserIdField, metrics.vuser.id)
      .addTag(VUserNameField, metrics.vuser.name)
      .addTag(SimulationIdField, metrics.simulation.id)
      .addTag(SimulationNameField, metrics.simulation.name)
      .addField(ElapsedTimeField, requestDurationMcs)
    metricsDb.write(point) map { _ => () }
  }

  def toNanoTimestamp(dateTime: OffsetDateTime) = {
    val utc = dateTime.atZoneSameInstant(ZoneOffset.UTC)
    utc.toEpochSecond * S_IN_NS + utc.getNano.toLong
  }
}
