package net.paoloambrosio.drizzle.feeder

import akka.testkit.{ImplicitSender, TestKit}
import net.paoloambrosio.drizzle.core.ScenarioContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Span.convertSpanToDuration
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.{CallingThreadExecutionContext, TestActorSystem}

import scala.concurrent.duration.FiniteDuration

class AkkaActorFeederActionFactorySpec extends TestKit(TestActorSystem()) with ImplicitSender
  with FlatSpecLike with Matchers with BeforeAndAfterAll with ScalaFutures {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "throw when the feed ends" in new TestContext {
    val action = feed(Seq.empty.toIterator)
    action(ScenarioContext()).failed.futureValue shouldBe an[IllegalStateException]
  }

  it should "add new variables to the session" in new TestContext {
    val beforeVariables = Map("a" -> "A", "b" -> 2)
    val action = feed(Seq(Map("a" -> 1)).toIterator)

    val afterVariables = action(ScenarioContext(sessionVariables = beforeVariables)).futureValue.sessionVariables

    afterVariables shouldBe Map("a" -> 1, "b" -> 2)
  }

  it should "allow different actions to use the same feeder" in new TestContext {
    val feeder = Seq(Map("a" -> 1), Map("b" -> 2)).toIterator

    feed(feeder)(ScenarioContext()).futureValue.sessionVariables shouldBe Map("a" -> 1)
    feed(feeder)(ScenarioContext()).futureValue.sessionVariables shouldBe Map("b" -> 2)
  }

  // HELPERS

  trait TestContext extends AkkaActorFeederActionFactory {
    override implicit val ec = new CallingThreadExecutionContext
    override val feederTimeout = convertSpanToDuration(patienceConfig.timeout).asInstanceOf[FiniteDuration]
    override val feederActor = system.actorOf(FeederActor.props)
  }

}
