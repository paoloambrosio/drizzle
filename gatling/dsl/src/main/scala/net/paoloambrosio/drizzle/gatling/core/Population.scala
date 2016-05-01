package net.paoloambrosio.drizzle.gatling.core

case class Population(
  scenario: Scenario,
  injectionSteps: Seq[InjectionStep],
  protocols: Seq[Protocol] = Seq.empty
) {
  def protocols(extraProtocols: Protocol*): Population = copy(protocols = protocols ++ extraProtocols)
}