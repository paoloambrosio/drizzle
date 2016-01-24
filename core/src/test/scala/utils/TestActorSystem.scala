package utils

import java.util.UUID

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object TestActorSystem {

  val testConfig = """|
                  |akka.loggers = ["akka.testkit.TestEventListener"]
                  |akka.stdout-loglevel = "OFF"
                  |akka.loglevel = "OFF"
                  |akka.actor.default-dispatcher.type = "akka.testkit.CallingThreadDispatcherConfigurator"
                  |akka.test.single-expect-default = 0
                  |""".stripMargin

  def apply(): ActorSystem = {
    ActorSystem(
      s"test-${UUID.randomUUID}",
      ConfigFactory.parseString(testConfig)
    )
  }
}
