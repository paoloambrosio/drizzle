package net.paoloambrosio.drizzle.metrics.influxdb

import java.time.format.DateTimeFormatter
import java.time.{OffsetDateTime => jOffsetDateTime, ZoneOffset}

import com.paulgoldbaum.influxdbclient.{Database, InfluxDB}
import common._
import net.paoloambrosio.drizzle.metrics._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

class InfluxDbMetricsSpec extends FlatSpec with Matchers with ScalaFutures with UnixFallbackDockerTestKit
  with DockerInfluxDBService {

  lazy val influxDbHost = docker.host
  lazy val influxDbPort = influxdbContainer.getPorts().futureValue.apply(InfluxDBAPI)
  lazy val influxdb = InfluxDB.connect(influxDbHost, influxDbPort)

  private val TIME_OFFSET = ZoneOffset.ofHoursMinutes(2, 30)

  lazy val metricsDb: Database = {
    val dbName = randomAlphaNumeric(10)
    val db = influxdb.selectDatabase(dbName)
    db.create().futureValue.series shouldBe List.empty
    db
  }

  lazy val influxDbMetrics = new InfluxDbMetrics(metricsDb)

  it should "write metrics for a single request" in {
    val run = SimulationMetrics("runIdX", absoluteStart = simulationRunStart)
    val vUser = VUserMetrics(run, "vUserIdY", start = 3 seconds)
    val request = TimedActionMetrics(vUser, "requestIdZ", start = 7 seconds, elapsedTime = 13 micros)

    val x = influxDbMetrics.store(request).futureValue

    val response = metricsDb.query("SELECT runId,vUserId,requestId,elapsedTime FROM action").futureValue
    val result = response.series.head
    parseTimes(result.points(0)) shouldBe requestsStart(request)
    result.points(1) shouldBe List(run.id)
    result.points(2) shouldBe List(vUser.id)
    result.points(3) shouldBe List(request.id)
    result.points(4) shouldBe requestsResponseTime(request)
  }

  private def parseTimes(values: List[Any]) = values map (v => parseTime(v.toString))
  private def parseTime(value: String) = jOffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME).withOffsetSameInstant(TIME_OFFSET)

  private def requestsStart(requests: TimedActionMetrics*) = requests map (_.absoluteStart)
  private def requestsResponseTime(requests: TimedActionMetrics*) = requests map (_.elapsedTime.getNano / 1000)

  // InfluxDB seems to ignore in queries data points that are less than 30 seconds in the past
  private val simulationRunStart = jOffsetDateTime.now().minus(5 minutes).withOffsetSameInstant(TIME_OFFSET)
}
