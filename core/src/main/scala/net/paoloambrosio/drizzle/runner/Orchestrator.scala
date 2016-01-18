package net.paoloambrosio.drizzle.runner

import akka.actor.{Actor, Props}
import net.paoloambrosio.drizzle.core._

object Orchestrator {

//  sealed trait State
//  case object Idle extends State      // VUsers not provisioned
//  case object WarmingUp extends State // VUsers being created
//  case object Running extends State   // VUsers running

  case class Start(scenarios: Seq[Scenario]) // IN start vusers for each scenario
  case object Finished                       // OUT all vusers stopped

  def props(): Props = Props(new Orchestrator)

}

class Orchestrator extends Actor {

  import Orchestrator._

  override def receive: Receive = {
    case Start(scenarios) => {
      if (scenarios.isEmpty)
        sender() ! Finished
      else
        scenarios.foreach { s => startVUser(s) }
    }
  }

  def startVUser(s: Scenario) {
    val vuser = context.actorOf(VUser.props(s))
    vuser ! VUser.Start
  }

}
