package net.paoloambrosio.drizzle.runner

import java.time.{Clock, Duration, OffsetDateTime}

import akka.actor._
import akka.pattern.pipe
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.metrics.TimedActionMetrics

import scala.concurrent.ExecutionContext

object VUser {

  sealed trait State
  case object Idle extends State
  case object Running extends State

  sealed trait Data
  case object Uninitialized extends Data
  final case class Initialised(orchestrator: ActorRef, scenario: Scenario) extends Data

  // IN
  final case class Start(scenario: Scenario)
  case object Stop
  private case class NextStep(context: ScenarioContext)

  // OUT
  case object Success
  final case class Failure(exception: Throwable)

  /**
    * Create Props for a VUser.
    *
    * @return a Props for creating a VUser
    */
  def props(metricsCollector: ActorRef) = Props(
    new VUser(Clock.systemUTC(), metricsCollector)
  )
}

class VUser(clock: Clock, metricsCollector: ActorRef) extends Actor with FSM[VUser.State, VUser.Data] {

  import VUser._

  implicit private val ec: ExecutionContext = context.dispatcher

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(Start(scenario), Uninitialized) =>
      val orchestrator = sender()
      self ! NextStep(initialContext)
      goto(Running) using Initialised(orchestrator, scenario)
  }

  when(Running) {
    case Event(NextStep(context), s @ Initialised(_, Scenario(_, nextStep #:: rest))) =>
      execAction(nextStep, context)
      stay using s.copy(scenario = s.scenario.copy(steps = rest))
    case Event(NextStep(context), Initialised(_, _)) =>
      stop()
    case Event(Stop, Initialised(_, _)) =>
      stop()
    case Event(Status.Failure(t), _) =>
      stop(FSM.Failure(t))
  }

  onTermination {
    case StopEvent(FSM.Normal, state, Initialised(orchestrator, _)) => orchestrator ! Success
    case StopEvent(FSM.Failure(cause: Throwable), state, Initialised(orchestrator, _)) =>  orchestrator ! Failure(cause)
  }

  initialize()

  private def initialContext = ScenarioContext(ActionTimers(OffsetDateTime.now(clock), Duration.ZERO))

  private def execAction(step: ScenarioStep, beginContext: ScenarioContext) = {
    step.action(beginContext) map { endContext =>
      sendMetrics(step.name, endContext.lastAction)
      NextStep(endContext)
    } pipeTo self
  }

  private def sendMetrics(stepName: Option[String], timers: ActionTimers) = stepName match {
    case Some(_) =>
      val metrics = TimedActionMetrics(null, null, null, timers.start, timers.elapsedTime) // TODO
      metricsCollector ! metrics
    case _ =>
  }
}
