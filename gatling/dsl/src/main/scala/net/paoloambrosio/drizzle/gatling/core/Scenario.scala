package net.paoloambrosio.drizzle.gatling.core

case class Scenario(name: String, actions: List[GatlingAction] = Nil) extends ActionSequenceBuilder[Scenario] {

  override def builderInstance(newActions: List[GatlingAction]): Scenario = copy(actions = newActions)

  def inject(injectionSteps: InjectionStep*): Population = Population(this, injectionSteps)
}
