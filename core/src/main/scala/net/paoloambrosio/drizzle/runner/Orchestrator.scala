package net.paoloambrosio.drizzle.runner

import akka.actor.{FSM, ActorRef, Actor, Props}
import net.paoloambrosio.drizzle.core._

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

  def props(): Props = Props(new Orchestrator(VUser.props()))

}

class Orchestrator(vuserProps: Props) extends Actor with FSM[Orchestrator.State, Orchestrator.Data] {

  import Orchestrator._

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(Start(scenarios), Uninitialized) =>
      val runner = sender()
      val vusers = scenarios.map(startVUser(_))
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

  private def startVUser(s: Scenario): ActorRef = {
    val vuser = context.actorOf(vuserProps)
    vuser ! VUser.Start(s)
    vuser
  }

}
