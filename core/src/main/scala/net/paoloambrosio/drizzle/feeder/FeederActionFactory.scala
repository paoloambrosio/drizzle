package net.paoloambrosio.drizzle.feeder

import net.paoloambrosio.drizzle.core.{ScenarioAction, SessionVariables}

trait FeederActionFactory {

  def feed(feeder: Iterator[SessionVariables]): ScenarioAction
}
