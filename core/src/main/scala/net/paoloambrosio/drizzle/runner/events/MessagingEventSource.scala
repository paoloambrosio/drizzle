package net.paoloambrosio.drizzle.runner.events

import akka.actor.ActorRef
import net.paoloambrosio.drizzle.core.ActionTimers
import net.paoloambrosio.drizzle.core.events.VUserEventSource

class MessagingEventSource(eventListeners: Seq[ActorRef]) extends VUserEventSource {

  override def fireVUserCreated() = fireEvent(VUserCreated)
  override def fireVUserStarted() = fireEvent(VUserStarted)
  override def fireVUserMetrics(at: ActionTimers) = fireEvent(VUserMetrics(at.start, at.elapsedTime))
  override def fireVUserStopped() = fireEvent(VUserStopped)
  override def fireVUserTerminated() = fireEvent(VUserTerminated)

  private def fireEvent(event: Any): Unit = eventListeners.foreach(_ ! event)

}
