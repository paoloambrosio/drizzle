package net.paoloambrosio.drizzle.throttler

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import net.paoloambrosio.drizzle.core.ScenarioAction
import net.paoloambrosio.drizzle.throttler.ThrottlingActor._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait AkkaActorThrottledActionFactory extends ThrottledActionFactory {

  implicit def ec: ExecutionContext
  def throttlerTimeout: FiniteDuration

  def throttlingActor: ActorRef

  override def throttle(action: ScenarioAction): ScenarioAction = { ctx =>
    implicit val timeout: Timeout = throttlerTimeout
    (throttlingActor ? ThrottlingRequest) map (_ => ctx) flatMap action
  }

}
