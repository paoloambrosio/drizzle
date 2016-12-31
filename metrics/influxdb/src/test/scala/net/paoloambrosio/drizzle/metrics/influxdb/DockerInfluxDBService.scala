package net.paoloambrosio.drizzle.metrics.influxdb

import com.whisk.docker.DockerReadyChecker.HttpResponseCode
import com.whisk.docker.{DockerContainer, DockerKit}

import scala.concurrent.duration._

trait DockerInfluxDBService extends DockerKit {

  val InfluxDBAPIPort = 8086
  val InfluxDBAdminPort = 8083

  val influxdbContainer = DockerContainer("tutum/influxdb:0.13")
    .withPorts(InfluxDBAdminPort -> None, InfluxDBAPIPort -> None)
    .withReadyChecker(
        HttpResponseCode(port=InfluxDBAPIPort, path="/ping", code=204)
          .looped(20, 500 millis) // Without this does it try only once?!
      )

  abstract override def dockerContainers: List[DockerContainer] =
    influxdbContainer :: super.dockerContainers
}
