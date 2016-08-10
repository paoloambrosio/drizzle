package net.paoloambrosio.drizzle.feeder

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout._
import net.paoloambrosio.drizzle.core._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

trait AkkaActorFeederActionFactory extends FeederActionFactory {

  import FeederActor._

  implicit def ec: ExecutionContext
  def feederTimeout: FiniteDuration

  def feederActor: ActorRef

  def feed(feeder: Feeder): ScenarioAction = { in: ScenarioContext =>
    implicit val timeout = durationToTimeout(feederTimeout)
    (feederActor ? FeedRequest(feeder)).mapTo[FeedResponse] flatMap {
      case FeedResponse(Some(vars)) =>
        val out = in.copy(sessionVariables = in.sessionVariables ++ vars)
        Future.successful(out)
      case FeedResponse(None) =>
        Future.failed(new IllegalStateException("Feeder source terminated"))
    }
  }
}