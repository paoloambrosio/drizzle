package net.paoloambrosio.drizzle.feeder

import akka.actor.{Actor, Props}
import net.paoloambrosio.drizzle.core._

object FeederActor {

  case class FeedRequest(feeder: Feeder)
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