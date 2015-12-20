package net.paoloambrosio.drizzle.metrics.influxdb

import java.time.format.DateTimeFormatter
import java.time.temporal.{ChronoUnit => jChronoUnit}
import java.time.{Duration => jDuration, OffsetDateTime => jOffsetDateTime, ZoneOffset}

import com.paulgoldbaum.influxdbclient.{Database, InfluxDB}
import common._
import net.paoloambrosio.drizzle.metrics.{Request, VUser}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

class InfluxDbMetricsSpec extends FlatSpec with Matchers with ScalaFutures with UnixFallbackDockerTestKit
  with DockerInfluxDBService {

  val RFC_3339_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

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

  it should "write a single request" in {
    val vUser = VUser(vUserStartTime)
    val request = Request(
        vUser,
        start=jDuration.of(42, jChronoUnit.SECONDS),
      elapsed=jDuration.of(13000, jChronoUnit.NANOS)
    )

    val x = influxDbMetrics.store(request).futureValue

    val response = metricsDb.query("SELECT elapsed_mcs FROM request").futureValue
    val result = response.series.head
    result.points(0) shouldBe timePoints(request)
    result.points(1) shouldBe elapsedMcsPoints(request)
  }

  private def timePoints(requests: Request*) = requests map (_.absoluteStart.atZoneSameInstant(ZoneOffset.UTC).format(RFC_3339_MS))
  private def elapsedMcsPoints(requests: Request*) = requests map (_.elapsed.getNano / 1000)

  // InfluxDB seems to ignore in queries data points that are less than 30 seconds in the past
  private val vUserStartTime = jOffsetDateTime.now().minus(42, jChronoUnit.SECONDS)
}
