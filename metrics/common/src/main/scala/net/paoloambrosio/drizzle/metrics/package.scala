package net.paoloambrosio.drizzle

import java.time._

package object metrics {

  type Id = String

  /**
    * One run of a simulation.
    *
    * @param id Unique identifier of this run
    * @param absoluteStart Absolute date and time when the simulation run was started
    */
  case class SimulationRun(id: Id, absoluteStart: OffsetDateTime)

  /**
    * Virtual user in a simulation run.
    *
    * @param run Simulation run this virtual user belongs to
    * @param id Unique identifier of this virtual user relative to the run
    * @param start Relative start time
    */
  case class VUser(run: SimulationRun, id: Id, start: Duration) {
    def absoluteStart = run.absoluteStart.plus(start)
  }

  /**
    * A single request
    *
    * @param vuser Virtual User that issued the request
    * @param id Request identifier
    * @param start Request offset from the start of the virtual user
    * @param responseTime Elapsed time between request and response
    */
  case class Request(vuser: VUser, id: Id, start: Duration, responseTime: Duration) {
    def absoluteStart = vuser.absoluteStart.plus(start)
  }

}
