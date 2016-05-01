package net.paoloambrosio.drizzle.gatling.core

trait Simulation {

  private var setUpBuilder = new SetUpBuilder()

  /*
   * Apparently it can be called more than once but the last call wins!
   */
  def setUp(populations: Population*): SetUpBuilder = {
    setUpBuilder = new SetUpBuilder(populations)
    setUpBuilder
  }

  private [core] def build = setUpBuilder.build
}
