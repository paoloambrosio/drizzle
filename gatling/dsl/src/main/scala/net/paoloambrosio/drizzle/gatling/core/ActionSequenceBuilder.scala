package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.feeder.Feeder

import scala.concurrent.duration._

trait ActionSequenceBuilder[T <: ActionSequenceBuilder[T]] {

  // Note: List and not Seq because of varargs and type erasure
  def builderInstance(actions: List[GatlingAction]): T
  def actions: List[GatlingAction]

  def exec(newActions: List[GatlingAction]): T = builderInstance(actions ++ newActions)
  def exec(newAction: GatlingAction): T = exec(List(newAction))
  def exec(builders: ActionSequenceBuilder[_]*): T = exec(builders.map(_.actions).flatten.toList)

  def pause(seconds: Int): T = pause(Duration(seconds, SECONDS))
  def pause(duration: Duration): T = exec(List(new PauseAction(duration)))

  def feed(feeder: Feeder): T = exec(List(new FeedingAction(feeder)))
}
