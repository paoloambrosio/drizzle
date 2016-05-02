package net.paoloambrosio.drizzle.http.action

import net.paoloambrosio.drizzle.core.ScenarioAction
import net.paoloambrosio.drizzle.http.model.HttpRequest

trait HttpActionFactory {

  def httpAction(request: HttpRequest): ScenarioAction
}
