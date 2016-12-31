package common

import com.spotify.docker.client.DefaultDockerClient
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.Suite

import scala.concurrent.duration._

/**
  * DockerTestKit configuration for Drizzle
  */
trait DrizzleDockerTestKit extends DockerTestKit { self: Suite =>

  // Wait a bit more when using dockerised services (five seconds to make the build more reliable on CI server)
  override implicit def patienceConfig = PatienceConfig(timeout = 5 seconds)

  override implicit val dockerFactory: DockerFactory =
    new SpotifyDockerFactory(DefaultDockerClient.fromEnv().build())
}
