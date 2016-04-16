package net.paoloambrosio.drizzle.core.events

import net.paoloambrosio.drizzle.core.ActionTimers

trait VUserEventSource {

  def fireVUserCreated()
  def fireVUserStarted()
  def fireVUserMetrics(actionTimers: ActionTimers)
  def fireVUserStopped()
  def fireVUserTerminated()

}