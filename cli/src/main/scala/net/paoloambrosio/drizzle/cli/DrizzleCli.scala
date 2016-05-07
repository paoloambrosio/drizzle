package net.paoloambrosio.drizzle.cli

import java.time.Clock

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import net.paoloambrosio.drizzle.core.ScenarioStreamFactory
import net.paoloambrosio.drizzle.core.action.{AkkaSchedulerSleepActionFactory, CoreActionFactory, JavaTimeTimedActionFactory}
import net.paoloambrosio.drizzle.core.events.VUserEventSource
import net.paoloambrosio.drizzle.http.action.AkkaHttpActionFactory
import net.paoloambrosio.drizzle.runner.Orchestrator
import net.paoloambrosio.drizzle.runner.events.MessagingEventSource

import scala.concurrent.Await
import scala.concurrent.duration._

abstract class DrizzleCli extends App with ScenarioStreamFactory
    with CoreActionFactory with AkkaHttpActionFactory
    with AkkaSchedulerSleepActionFactory with JavaTimeTimedActionFactory {
    this: SimulationLoader =>

  if (args.length != 1) {
    System.err.println("One single parameter needs to be specified")
    System.exit(1)
  }

  val config = ConfigFactory.load()
  override implicit final val system = ActorSystem("drizzle-cli", config)
  override implicit final val materializer = ActorMaterializer()
  override final val ec = system.dispatcher
  override final val scheduler = system.scheduler
  override final val clock: Clock = Clock.systemUTC()

  private lazy val progressPrinter = system.actorOf(CliProgressPrinter.props())
  override lazy val vUserEventSource: VUserEventSource = new MessagingEventSource(Seq(progressPrinter))
  private lazy val orchestrator = system.actorOf(Orchestrator.props(vUserEventSource), "orchestrator")

  private val durationTimeout = 1 hour

  val scenarios = scenarioStream(load(args(0)).scenarioProfiles)
  val result = orchestrator.ask(Orchestrator.Start(scenarios))(durationTimeout)
  Await.result(result, durationTimeout)
  system.terminate()
}
