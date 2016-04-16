package net.paoloambrosio.drizzle.runner

import java.time.{Duration, OffsetDateTime}

package object events {

  case object VUserCreated
  case object VUserStarted
  case class VUserMetrics(start: OffsetDateTime, elapsedTime: Duration)
  case object VUserStopped
  case object VUserTerminated

}