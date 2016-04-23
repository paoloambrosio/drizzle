package net.paoloambrosio.drizzle.cli

import java.time.Clock

import akka.actor.{ActorSystem, Scheduler}
import akka.pattern.ask
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.{AkkaSchedulerSleepActionFactory, JavaTimeTimedActionFactory}
import net.paoloambrosio.drizzle.core.events.VUserEventSource
import net.paoloambrosio.drizzle.feeder.{AkkaActorFeederActionFactory, FeederActor}
import net.paoloambrosio.drizzle.runner.Orchestrator
import net.paoloambrosio.drizzle.runner.events.MessagingEventSource
import net.paoloambrosio.drizzle.throttler.{AkkaActorThrottledActionFactory, FixedWindowThrottler, ThrottlingActor}
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

trait DrizzleApp extends App with LoadInjectionStepsFactory
  with ScenarioStreamFactory with AkkaSchedulerSleepActionFactory {

  final val system = ActorSystem("drizzle")

  override final implicit lazy val ec: ExecutionContext = system.dispatcher
  override final implicit lazy val scheduler: Scheduler = system.scheduler

  private val actionFactory = new AkkaSchedulerSleepActionFactory
    with JavaTimeTimedActionFactory with AkkaActorFeederActionFactory with AkkaActorThrottledActionFactory {

    override final implicit lazy val ec: ExecutionContext = system.dispatcher
    override final implicit lazy val scheduler: Scheduler = system.scheduler
    override final val clock: Clock = Clock.systemDefaultZone()
    override final lazy val feederActor = system.actorOf(FeederActor.props, "feeder")
    override def feederTimeout: FiniteDuration = durationTimeout
    override final lazy val throttlingActor = system.actorOf(ThrottlingActor.props(new FixedWindowThrottler(throttlingPattern)), "throttler")
    override def throttlerTimeout: FiniteDuration = durationTimeout
  }

  private lazy val progressPrinter = system.actorOf(CliProgressPrinter.props())
  override final lazy val vUserEventSource: VUserEventSource = new MessagingEventSource(Seq(progressPrinter))
  private lazy val orchestrator = system.actorOf(Orchestrator.props(vUserEventSource), "orchestrator")

  private val durationTimeout = 1 hour

  val result = orchestrator.ask(Orchestrator.Start(scenarioStream(simulation)))(durationTimeout)
  Await.result(result, durationTimeout)
  system.terminate()

  def simulation: Seq[ScenarioProfile]
  def throttlingPattern: Stream[Int] // for now just produce the max number of requests per second

  // DSL

  def exec(name: String, f: SessionVariables => Future[SessionVariables]) = {
    ScenarioStep(Some(name), actionFactory.throttle(actionFactory.timedAction(f)))
  }

  def sleep(name: String, t: FiniteDuration) = {
    ScenarioStep(Some(name), actionFactory.thinkTime(t))
  }

  def feed(feeder: Iterator[SessionVariables]) = {
    ScenarioStep(None, actionFactory.feed(feeder))
  }

  def scenario(name: String, steps: ScenarioStep*) = {
    Scenario(name, steps.toStream)
  }
}