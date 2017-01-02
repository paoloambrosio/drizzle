package net.paoloambrosio.drizzle.gatling.core

trait ChainBuildingSupport extends ActionSequenceBuilder[ChainBuilder] {

  def builderInstance(actions: List[GatlingAction]) = ChainBuilder(actions)
  def actions: List[GatlingAction] = Nil
}

case class ChainBuilder(actions: List[GatlingAction] = Nil) extends ActionSequenceBuilder[ChainBuilder] {
  override def builderInstance(actions: List[GatlingAction]): ChainBuilder = ChainBuilder(actions)
}