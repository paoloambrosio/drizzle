package net.paoloambrosio.drizzle.feeder

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout._
import net.paoloambrosio.drizzle.core._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

trait AkkaActorFeederActionFactory extends FeederActionFactory {

  import FeederActor._

  implicit def ec: ExecutionContext
  def feederTimeout: FiniteDuration

  def feederActor: ActorRef

  def feed(feeder: Iterator[SessionVariables]): ScenarioAction = { in: ScenarioContext =>
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

object FeederActor {

  case class FeedRequest(feeder: Iterator[SessionVariables])
  case class FeedResponse(vars: Option[SessionVariables])

  def props = Props(new FeederActor)
}

class FeederActor extends Actor {
  import FeederActor._

  override def receive: Receive = {
    case FeedRequest(feeder) if feeder.hasNext =>
      sender() ! FeedResponse(Some(feeder.next()))
    case FeedRequest(_) =>
      sender() ! FeedResponse(None)
  }
}