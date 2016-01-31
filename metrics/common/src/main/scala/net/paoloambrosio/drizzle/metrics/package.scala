package net.paoloambrosio.drizzle

import java.time._

package object metrics {

  type Id = String

  /**
    * Metrics for a simulation run.
    *
    * @param id Unique identifier of this run
    * @param absoluteStart Absolute date and time when the simulation run was started
    */
  case class SimulationMetrics(id: Id, absoluteStart: OffsetDateTime)

  /**
    * Metrics for a Virtual user in a simulation run.
    *
    * @param run Simulation run this virtual user belongs to
    * @param id Unique identifier of this virtual user relative to the run
    * @param start Relative start time of the first timed action
    */
  case class VUserMetrics(run: SimulationMetrics, id: Id, start: Duration) {
    def absoluteStart = run.absoluteStart.plus(start)
  }

  /**
    * Metrics for a single timed action from a VUser.
    *
    * @param vuser Virtual User that issued the request
    * @param id Request identifier
    * @param start Request offset from the start of the virtual user
    * @param elapsedTime Elapsed time between request and response
    */
  case class TimedActionMetrics(vuser: VUserMetrics, id: Id, start: Duration, elapsedTime: Duration) {
    def absoluteStart = vuser.absoluteStart.plus(start)
  }

}
