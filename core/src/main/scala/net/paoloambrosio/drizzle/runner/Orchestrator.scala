package net.paoloambrosio.drizzle.runner

import akka.actor.{Props, Actor}
import net.paoloambrosio.drizzle.core._

object Orchestrator {

  def props(scenario: Scenario, loadProfile: LoadProfile): Props = {
    Props(new Orchestrator(scenario, loadProfile))
  }
}

// TODO it should be more than one scenario + load profile!!!!
class Orchestrator(scenario: Scenario, loadProfile: LoadProfile) extends Actor {

  override def receive: Receive = ???

}
