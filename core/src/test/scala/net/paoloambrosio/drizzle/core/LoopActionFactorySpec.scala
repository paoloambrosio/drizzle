package net.paoloambrosio.drizzle.core

import net.paoloambrosio.drizzle.core.action.LoopActionFactory
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class LoopActionFactorySpec extends FlatSpec with Matchers with MockitoSugar {

  "forever" should "create an infinite loop" in new TestContext {
    val loop = forever(Stream(a1, a2, a3))

    loop.take(42).length shouldBe 42
    loop.take(8) shouldBe Stream(a1, a2, a3, a1, a2, a3, a1, a2)
  }

  "repeat" should "multiply the stream" in new TestContext {
    val loop = repeat(3)(Stream(a1, a2, a3))

    loop shouldBe Stream(a1, a2, a3, a1, a2, a3, a1, a2, a3)
  }

  // HELPERS

  trait TestContext extends LoopActionFactory {
    val a1: ScenarioAction = x => Future.successful(x)
    val a2: ScenarioAction = x => Future.successful(x)
    val a3: ScenarioAction = x => Future.successful(x)
  }

}
