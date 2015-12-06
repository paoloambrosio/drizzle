package net.paoloambrosio.drizzle.metrics.influxdb

import com.whisk.docker.DockerReadyChecker.HttpResponseCode
import com.whisk.docker.{DockerContainer, DockerKit}

import scala.concurrent.duration._

trait DockerInfluxDBService extends DockerKit {

  val InfluxDBAPI = 8086
  val InfluxDBAdmin = 8083

  val influxdbContainer = DockerContainer("tutum/influxdb:0.9")
    .withPorts(InfluxDBAdmin -> None, InfluxDBAPI -> None)
    .withReadyChecker(
        HttpResponseCode(port=InfluxDBAPI, path="/ping", code=204)
          .looped(20, 500 millis) // Without this does it try only once?!
      )

  abstract override def dockerContainers: List[DockerContainer] =
    influxdbContainer :: super.dockerContainers
}
