package net.paoloambrosio.drizzle.gatling.core

import org.scalatest.{FlatSpecLike, Matchers}

class ChainBuildingSupportSpec extends FlatSpecLike with Matchers {

  it should "queue an action" in new TestContext {
    exec(Action1).actions shouldBe List(Action1)
  }

  it should "chain multiple actions" in new TestContext {
    exec(Action1).exec(Action2).exec(Action3).actions shouldBe List(Action1, Action2, Action3)
  }

  // HELPERS

  trait TestContext extends ChainBuildingSupport {
    case object Action1 extends GatlingAction
    case object Action2 extends GatlingAction
    case object Action3 extends GatlingAction
  }
}
