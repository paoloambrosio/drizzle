package net.paoloambrosio.drizzle.http.action

import net.paoloambrosio.drizzle.core.ScenarioAction
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.PostTimedPart
import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.http.checks.HttpCheck
import net.paoloambrosio.drizzle.http.{HttpRequest, HttpResponse}

trait HttpActionFactory {

  def httpRequest(request: Expression[HttpRequest], checks: Seq[HttpCheck]): ScenarioAction

}
