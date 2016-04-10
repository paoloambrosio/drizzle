package net.paoloambrosio.drizzle.runner

import java.time.Clock

import akka.actor.{Actor, ActorRef, FSM, Props}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.metrics.{RuntimeInfo, TimedActionMetrics}
import net.paoloambrosio.drizzle.runner.MetricsWriter.VUserMetrics

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

  def props(metricsCollectors: Seq[ActorRef]): Props = Props(
    new Orchestrator(Clock.systemUTC(), metricsCollectors, VUser.props)
  )

}

class Orchestrator(clock: Clock, metricsCollectors: Seq[ActorRef], vuserProps: Props)
    extends Actor with FSM[Orchestrator.State, Orchestrator.Data] {

  import Orchestrator._

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(Start(scenarios), Uninitialized) =>
      val runner = sender()
      val vusers = scenarios.map(startNewVUser(_))
      updateState(runner, vusers)
  }

  when(Running) {
    case Event(VUser.Success | VUser.Failure(_), Initialised(runner, vusers)) =>
      val vusersLeft = vusers.filterNot(_ == sender())
      updateState(runner, vusersLeft)
  }

  initialize()

  private def updateState(runner: ActorRef, vusers: Seq[ActorRef]) = {
    if (vusers.isEmpty) {
      runner ! Finished
      stop(FSM.Normal, Uninitialized)
    } else {
      goto(Running) using Initialised(runner, vusers)
    }
  }

  private def startNewVUser(scenario: Scenario): ActorRef = {
    val vuser = context.actorOf(vuserProps)
    vuser ! VUser.Start(wrapStepsSendingMetrics(scenario.steps))
    vuser
  }

  private def wrapStepsSendingMetrics(steps: Stream[ScenarioStep]) = {
    steps.map(s => s.copy(action = wrapActionSendingMetrics(s.action)))
  }

  private def wrapActionSendingMetrics(action: ScenarioAction): ScenarioAction = in => {
    implicit val executor = context.dispatcher // FIXME?
    action(in) map { out => out.latestAction match {
      case Some(at) => sendMetrics(at); out
      case None => out
    }}
  }

  private def sendMetrics(at: ActionTimers): Unit = {
    val todoRuntimeInfo = RuntimeInfo(None,"")
    metricsCollectors.foreach { mc =>
      mc ! VUserMetrics(at.start, at.elapsedTime)
    }
  }

}
