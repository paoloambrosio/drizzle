package net.paoloambrosio.drizzle.runner

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.{FlatSpecLike, Matchers}

class OrchestratorSpec extends TestKit(ActorSystem("mock-scheduler")) with FlatSpecLike
    with Matchers with AsyncAssertions {

  it should "do stuff" in new TestContext {
  }

  trait TestContext {
    val w = new Waiter
  }
}
