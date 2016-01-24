package net.paoloambrosio.drizzle.runner

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import net.paoloambrosio.drizzle.core.{Scenario, ScenarioStep}
import net.paoloambrosio.drizzle.runner.Orchestrator._
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.TestActorSystem


class OrchestratorSpec extends TestKit(TestActorSystem()) with ImplicitSender
    with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "finish immediately if there is nothing to do" in new TestContext {
    val orchestrator = actor()

    orchestrator ! Start(Seq.empty[Scenario])

    startedVusers shouldBe empty
    expectMsg(Finished)
  }

  it should "start a vuser for each passed scenario" in new TestContext {
    val orchestrator = actor()
    val scenarios = Seq.fill(3)(SomeScenario)

    orchestrator ! Start(scenarios)

    startedVusers.size shouldBe scenarios.size
    expectNoMsg()
  }

  it should "finish when all vusers exit" in new TestContext {
    val orchestrator = actor()
    val scenarios = Seq.fill(3)(SomeScenario)

    orchestrator ! Start(scenarios)

    val (half, theOtherHalf) = splitVusers()
    expectNoMsg()
    half.foreach(orchestrator.tell(VUser.Success, _))
    expectNoMsg()
    theOtherHalf.foreach(orchestrator.tell(VUser.Failure(new Exception("BOOM!")), _))
    expectMsg(Finished)
  }

  // HELPERS

  trait TestContext {

    val SomeScenario = Scenario("", Stream.empty[ScenarioStep])

    var startedVusers = Seq.empty[ActorRef]
    def splitVusers() = startedVusers.splitAt(startedVusers.size/2)

    val testVUserProps = Props(new Actor {
      override def receive: Receive = {
        case VUser.Start(_) => startedVusers +:= self
      }
    })

    def actor() = {
      TestActorRef(new Orchestrator(testVUserProps))
    }
  }

}
