package net.paoloambrosio.drizzle

import java.time._

package object metrics {

  case class RuntimeInfo(name: Option[String],  id: String)

  /**
    * Metrics for a single timed action from a VUser.
    *
    * @param simulation Simulation this virtual user belongs to
    * @param vuser Virtual User that executed this action
    * @param action Action unique identifier
    * @param start Action start time
    * @param elapsedTime Elapsed time between beginning and end of the action
    */
  case class TimedActionMetrics(simulation: RuntimeInfo, vuser: RuntimeInfo, action: RuntimeInfo,
                                start: OffsetDateTime, elapsedTime: Duration)

}
