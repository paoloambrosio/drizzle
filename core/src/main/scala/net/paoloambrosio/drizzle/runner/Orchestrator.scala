package net.paoloambrosio.drizzle.runner

import java.time.{OffsetDateTime, Clock}

import akka.actor.{Actor, ActorRef, FSM, Props}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.metrics.SimulationMetrics

object Orchestrator {

  sealed trait State
  case object Idle extends State      // VUsers not provisioned
  case object Running extends State   // VUsers running

  sealed trait Data
  case object Uninitialized extends Data
  final case class Initialised(runner: ActorRef, vusers: Seq[ActorRef]) extends Data

  // IN
  final case class Start(scenarios: Seq[Scenario])

  // OUT
  case object Finished // TODO track if the run was successful or not returning "stats"

  def props(): Props = Props(new Orchestrator(Clock.systemUTC(), MetricsCollector.props(Seq.empty), VUser.props))

}

class Orchestrator(clock: Clock,
                   metricsProps: SimulationMetrics => Props,
                   vuserProps: ActorRef => Props
                  ) extends Actor with FSM[Orchestrator.State, Orchestrator.Data] {

  import Orchestrator._

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(Start(scenarios), Uninitialized) =>
      val runner = sender()
      val metricsCollector = newMetricsCollector()
      val vusers = scenarios.map(startVUser(_, metricsCollector))
      actOn(runner, vusers)
  }

  when(Running) {
    case Event(VUser.Success | VUser.Failure(_), Initialised(runner, vusers)) =>
      val vusersLeft = vusers.filterNot(_ == sender())
      actOn(runner, vusersLeft)
  }

  initialize()

  private def actOn(runner: ActorRef, vusers: Seq[ActorRef]) = {
    if (vusers.isEmpty) {
      runner ! Finished
      stop(FSM.Normal, Uninitialized)
    } else {
      goto(Running) using Initialised(runner, vusers)
    }
  }

  private def newMetricsCollector(): ActorRef = {
    val simulationMetrics = SimulationMetrics("", OffsetDateTime.now(clock))
    context.actorOf(metricsProps(simulationMetrics))
  }

  private def startVUser(scenario: Scenario, metricsCollector: ActorRef): ActorRef = {
    val vuser = context.actorOf(vuserProps(metricsCollector))
    vuser ! VUser.Start(scenario)
    vuser
  }

}
