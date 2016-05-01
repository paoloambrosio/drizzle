package net.paoloambrosio.drizzle.gatling.core

case class SetUp(
  populations: Seq[Population],
  protocols: Seq[Protocol]
)

/**
  * Builder for the test run. It must be mutable because of how the
  * Gatling DSL is defined [[Simulation.setUp]]
  *
  * @param populations
  */
class SetUpBuilder(
  private val populations: Seq[Population] = Seq.empty
) {

  private var protocols: Seq[Protocol] = Seq.empty

  def protocols(protocols: Protocol*): Unit = {
    this.protocols = protocols
  }

  def build: SetUp = SetUp(populations, protocols)
}
