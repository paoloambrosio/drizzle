package net.paoloambrosio.drizzle.gatling.core

import scala.concurrent.duration._

case class Scenario(
   name: String,
   actions: Seq[Action] = Seq.empty
) {

  def exec(action: Action): Scenario = copy(actions = actions :+ action)

  def pause(seconds: Int): Scenario = pause(Duration(seconds, SECONDS))
  def pause(duration: Duration): Scenario = exec(new PauseAction(duration))

  def inject(injectionSteps: InjectionStep*): Population = Population(this, injectionSteps)
}
