package net.paoloambrosio.drizzle.cli

import java.time.Clock

import akka.actor.{ActorSystem, Scheduler}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.{AkkaSchedulerSleepActionFactory, JavaTimeTimedActionFactory}
import net.paoloambrosio.drizzle.runner.Orchestrator
import akka.pattern.ask
import net.paoloambrosio.drizzle.core.events.VUserEventSource
import net.paoloambrosio.drizzle.runner.events.MessagingEventSource

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

trait DrizzleApp extends App with LoadInjectionStepsFactory
  with ScenarioStreamFactory with AkkaSchedulerSleepActionFactory with JavaTimeTimedActionFactory {

  override final val clock: Clock = Clock.systemDefaultZone()
  override final implicit lazy val ec: ExecutionContext = system.dispatcher
  override final implicit lazy val scheduler: Scheduler = system.scheduler
  override final lazy val vUserEventSource: VUserEventSource = new MessagingEventSource(Seq(progressPrinter))

  def timeout = 1 hour
  def simulation: Seq[ScenarioProfile]

  final val system = ActorSystem("drizzle")

  private lazy val progressPrinter = system.actorOf(CliProgressPrinter.props())
  private lazy val orchestrator = system.actorOf(Orchestrator.props(vUserEventSource), "orchestrator")

  val result = orchestrator.ask(Orchestrator.Start(scenarioStream(simulation)))(timeout)
  Await.result(result, timeout)
  system.terminate()
}
