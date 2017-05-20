package net.paoloambrosio.drizzle.gatling.core

import java.util.UUID

import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.feeder.Feeder

import scala.concurrent.duration._

trait ActionSequenceBuilder[T <: ActionSequenceBuilder[T]] {

  // TODO Hide these methods from the DSL
  // Note: List and not Seq because of varargs and type erasure
  def builderInstance(actions: List[GatlingAction]): T
  def actions: List[GatlingAction]

  def exec(newActions: List[GatlingAction]): T = builderInstance(actions ++ newActions)
  def exec(newAction: GatlingAction): T = exec(List(newAction))
  def exec(builders: ActionSequenceBuilder[_]*): T = exec(builders.map(_.actions).flatten.toList)

  def pause(seconds: Int): T = pause(Duration(seconds, SECONDS))
  def pause(duration: Duration): T = exec(List(PauseAction(duration)))

  def feed(feeder: Feeder): T = exec(List(FeedingAction(feeder)))

  private def defaultCounter = UUID.randomUUID.toString
  def repeat(times: Expression[Int], counterName: String = defaultCounter)(chain: T): T =
    exec(List(LoopAction(times, counterName, chain.actions)))
}
