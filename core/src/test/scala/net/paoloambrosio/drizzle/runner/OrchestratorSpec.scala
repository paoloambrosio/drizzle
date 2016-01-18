package net.paoloambrosio.drizzle.runner

import akka.actor.ActorSystem
import akka.testkit.{CallingThreadDispatcher, ImplicitSender, TestKit}
import net.paoloambrosio.drizzle.core.Scenario
import net.paoloambrosio.drizzle.runner.Orchestrator._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}


class OrchestratorSpec extends TestKit(ActorSystem("test-system")) with ImplicitSender
    with FlatSpecLike with Matchers with BeforeAndAfterAll with Eventually {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "exit immediately if there is nothing to do" in new TestContext {
    val orchestrator = actor()

    orchestrator ! Start(Seq.empty[Scenario])

    expectMsg(Finished)
  }

// TODO
//  it should "start a vuser for each passed scenario" in new TestContext {
//    val orchestrator = actor()
//
//    orchestrator ! Start(Seq(...))
//
//    expectMsg(Stopped)
//  }

  // HELPERS

  trait TestContext {

    def actor() = {
      system.actorOf(props().withDispatcher(CallingThreadDispatcher.Id))
    }
  }

}
