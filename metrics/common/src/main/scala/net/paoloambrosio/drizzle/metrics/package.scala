package net.paoloambrosio.drizzle

import java.time._

package object metrics {

//  type Id = String
//
//  /**
//    * One run of a simulation.
//    *
//    * @param id Unique identifier of this run
//    * @param start Absolute time when the simulation run was started
//    */
//  case class SimulationRun(id: Id, start: Instant)
//
//  /**
//    * Virtual user in a simulation run.
//    *
//    * @param run Simulation run this virtual user belongs to
//    * @param id Unique identifier of this virtual user relative to the run
//    * @param start Relative start time
//    */
//  case class VUser(run: SimulationRun, id: Id, start: OffsetDateTime) //
  case class VUser(start: OffsetDateTime) {
    def absoluteStart = start
  }

//  /**
//    * A single request
//    *
//    * @param vuser Virtual User that issued the request
//    * @param id Request identifier
//    * @param start Request offset from the start of the virtual user
//    * @param duration Elapsed time between request and response
//    */
//  class Request(vuser: VUser, id: Id, start: Duration, duration: Duration)
  case class Request(vuser: VUser, start: Duration, elapsed: Duration) {
    def absoluteStart = vuser.absoluteStart.plus(start)
  }

}
