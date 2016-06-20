package net.paoloambrosio.drizzle.gatling.core

trait ChainBuildingSupport extends ActionSequenceBuilder[ChainBuilder] {

  def builderInstance(actions: List[Action]) = ChainBuilder(actions)
  def actions: List[Action] = Nil
}

case class ChainBuilder(actions: List[Action] = Nil) extends ActionSequenceBuilder[ChainBuilder] {
  override def builderInstance(actions: List[Action]): ChainBuilder = ChainBuilder(actions)
}