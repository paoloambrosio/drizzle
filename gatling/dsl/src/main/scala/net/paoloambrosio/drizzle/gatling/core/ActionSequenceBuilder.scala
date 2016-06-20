package net.paoloambrosio.drizzle.gatling.core

import scala.concurrent.duration._

trait ActionSequenceBuilder[T <: ActionSequenceBuilder[T]] {

  // Note: List and not Seq because of varargs and type erasure
  def builderInstance(actions: List[Action]): T
  def actions: List[Action]

  def exec(newActions: List[Action]): T = builderInstance(actions ++ newActions)
  def exec(newAction: Action): T = exec(List(newAction))
  def exec(builders: ActionSequenceBuilder[_]*): T = exec(builders.map(_.actions).flatten.toList)

  def pause(seconds: Int): T = pause(Duration(seconds, SECONDS))
  def pause(duration: Duration): T = exec(List(new PauseAction(duration)))
}
