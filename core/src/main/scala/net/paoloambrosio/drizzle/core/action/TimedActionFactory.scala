package net.paoloambrosio.drizzle.core.action

import net.paoloambrosio.drizzle.core._

import scala.concurrent.Future

trait TimedActionFactory {

  def timedAction(f: SessionVariables => Future[SessionVariables]): ScenarioAction
}