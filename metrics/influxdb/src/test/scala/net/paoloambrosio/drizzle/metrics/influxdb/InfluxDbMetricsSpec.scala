package net.paoloambrosio.drizzle.metrics.influxdb

import java.time.format.DateTimeFormatter
import java.time.{OffsetDateTime => jOffsetDateTime}

import scala.concurrent.duration._

import com.paulgoldbaum.influxdbclient.{Database, InfluxDB}
import common._
import net.paoloambrosio.drizzle.metrics._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

class InfluxDbMetricsSpec extends FlatSpec with Matchers with ScalaFutures with UnixFallbackDockerTestKit
  with DockerInfluxDBService {

  lazy val influxDbHost = docker.host
  lazy val influxDbPort = influxdbContainer.getPorts().futureValue.apply(InfluxDBAPI)
  lazy val influxdb = InfluxDB.connect(influxDbHost, influxDbPort)

  lazy val metricsDb: Database = {
    val dbName = randomAlphaNumeric(10)
    val db = influxdb.selectDatabase(dbName)
    db.create().futureValue.series shouldBe List.empty
    db
  }

  lazy val influxDbMetrics = new InfluxDbMetrics(metricsDb)

  it should "write metrics for a single request" in {
    val run = SimulationRun("runIdX", absoluteStart = simulationRunStart)
    val vUser = VUser(run, "vUserIdY", start = 3 seconds)
    val request = Request(vUser, "requestIdZ", start = 7 seconds, responseTime = 13 micros)

    val x = influxDbMetrics.store(request).futureValue

    val response = metricsDb.query("SELECT runId,vUserId,requestId,responseTime FROM request").futureValue
    val result = response.series.head
    parseTimes(result.points(0)) shouldBe requestsStart(request)
    result.points(1) shouldBe List(run.id)
    result.points(2) shouldBe List(vUser.id)
    result.points(3) shouldBe List(request.id)
    result.points(4) shouldBe requestsResponseTime(request)
  }

  private def parseTimes(values: List[Any]) = values map (v => parseTime(v.toString))
  private def parseTime(value: String) = jOffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)

  private def requestsStart(requests: Request*) = requests map (_.absoluteStart)
  private def requestsResponseTime(requests: Request*) = requests map (_.responseTime.getNano / 1000)

  // InfluxDB seems to ignore in queries data points that are less than 30 seconds in the past
  private val simulationRunStart = jOffsetDateTime.now().minus(5 minutes)
}
