package utils

import java.util.UUID

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object TestActorSystem {

  val testConfig = """|
                  |akka.loggers = ["akka.testkit.TestEventListener"]
                  |akka.stdout-loglevel = "OFF"
                  |akka.loglevel = "OFF"
                  |""".stripMargin

  def apply(): ActorSystem = {
    ActorSystem(
      s"test-${UUID.randomUUID}",
      ConfigFactory.parseString(testConfig)
    )
  }
}
