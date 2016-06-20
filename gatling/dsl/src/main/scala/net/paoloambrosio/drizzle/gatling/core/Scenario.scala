package net.paoloambrosio.drizzle.gatling.core

case class Scenario(name: String, actions: List[Action] = Nil) extends ActionSequenceBuilder[Scenario] {

  override def builderInstance(newActions: List[Action]): Scenario = copy(actions = newActions)

  def inject(injectionSteps: InjectionStep*): Population = Population(this, injectionSteps)
}
