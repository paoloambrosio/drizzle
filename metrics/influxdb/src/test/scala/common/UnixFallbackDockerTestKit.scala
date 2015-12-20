package common

import com.github.dockerjava.core.DockerClientConfig
import com.whisk.docker.Docker
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.Suite

import scala.concurrent.duration._

/**
  * DockerTestKit with fallback to Unix socket unless DOCKER_HOST environment variable is present.
  */
trait UnixFallbackDockerTestKit extends DockerTestKit { self: Suite =>

  // Wait a bit more when using dockerised services (five seconds to make the build more reliable on Circle-CI)
  override implicit def patienceConfig = PatienceConfig(timeout = 5 seconds)

  override implicit val docker: Docker = {
    val builder = DockerClientConfig.createDefaultConfigBuilder()
    if (System.getenv("DOCKER_HOST") == null)
      builder.withUri("unix:///var/run/docker.sock")
    new Docker(builder.build())
  }

}
