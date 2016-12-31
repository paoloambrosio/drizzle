package net.paoloambrosio.drizzle.metrics.influxdb

import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, Duration => jDuration, OffsetDateTime => jOffsetDateTime}

import com.paulgoldbaum.influxdbclient.{Database, InfluxDB}
import common._
import net.paoloambrosio.drizzle.metrics._
import net.paoloambrosio.drizzle.metrics.influxdb.InfluxDbMetrics._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

class InfluxDbMetricsSpec extends FlatSpec with Matchers with ScalaFutures with DrizzleDockerTestKit
  with DockerInfluxDBService {

  lazy val influxDbHost = influxdbContainer.getIpAddresses().futureValue.head
  lazy val influxdb = InfluxDB.connect(influxDbHost, InfluxDBAPIPort)

  private val TIME_OFFSET = ZoneOffset.ofHoursMinutes(2, 30)

  lazy val metricsDb: Database = {
    val dbName = randomAlphaNumeric(10)
    val db = influxdb.selectDatabase(dbName)
    db.create().futureValue.series shouldBe List.empty
    db
  }

  lazy val influxDbMetrics = new InfluxDbMetrics(metricsDb)

  it should "write metrics for a single request" in {
    val metrics = TimedActionMetrics(
      simulation = RuntimeInfo(Some("simulation"), "runIdX"),
      vuser = RuntimeInfo(Some("vuser"), "vuserIdY"),
      action = RuntimeInfo(Some("action"), "requestIdZ"),
      start = simulationRunStart,
      elapsedTime = 13 micros)

    val x = influxDbMetrics

    influxDbMetrics.store(metrics).futureValue

    val response = metricsDb.query(s"SELECT * FROM $ActionType").futureValue
    val result = response.series.head
    parseTimes(result.points(0)) shouldBe List(metrics.start)
    result.points(ActionIdField) shouldBe List(metrics.action.id)
    result.points(ActionNameField) shouldBe List(metrics.action.name.get)
    result.points(VUserIdField) shouldBe List(metrics.vuser.id)
    result.points(VUserNameField) shouldBe List(metrics.vuser.name.get)
    result.points(SimulationIdField) shouldBe List(metrics.simulation.id)
    result.points(SimulationNameField) shouldBe List(metrics.simulation.name.get)
    result.points(ElapsedTimeField) shouldBe List(micros(metrics.elapsedTime))
  }

  private def parseTimes(values: List[Any]) = values map (v => parseTime(v.toString))
  private def parseTime(value: String) = jOffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME).withOffsetSameInstant(TIME_OFFSET)
  private def micros(d: jDuration) = d.toNanos / 1000

  // InfluxDB seems to ignore in queries data points that are less than 30 seconds in the past
  private val simulationRunStart = jOffsetDateTime.now().minus(5 minutes).withOffsetSameInstant(TIME_OFFSET)
}
