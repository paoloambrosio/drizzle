package net.paoloambrosio.drizzle.metrics.influxdb

import com.paulgoldbaum.influxdbclient.{Database, InfluxDB}
import common.UnixFallbackDockerTestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}

import common.randomAlphaNumeric

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

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  it should "find the created database" in {
    // verifies that everything is ready to test the implementation
    metricsDb.exists().futureValue shouldBe true
  }

}
