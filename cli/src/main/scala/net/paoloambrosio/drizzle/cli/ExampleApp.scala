package net.paoloambrosio.drizzle.cli

import net.paoloambrosio.drizzle.core.{Scenario, ScenarioProfile}
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object ExampleApp extends DrizzleApp {

  def printChar(s: String) = exec(s"print$s", vars => {
    val n = vars("N")
    println(s"$s[$n]")
    Future.successful(vars)
  })

  // use def to have a different feed each time, lazy val for the same feed across all scenarios
  lazy val randomCharFeeder = Stream.from(1).map(n => Map("N" -> n)).toIterator

  override def simulation = Seq(
    ScenarioProfile(
      scenario("A",
        feed(randomCharFeeder), printChar("A1"), sleep("wait", 2 seconds), printChar("A2")
      ),
      rampUsers(4, 4 second)),
    ScenarioProfile(scenario("B",
      feed(randomCharFeeder), printChar("B1"), sleep("wait", 2 seconds), printChar("B2")
    ), rampUsers(4, 10 seconds))
  )

  override lazy val throttlingPattern = Stream.fill(5)(0) #::: Stream.continually(Int.MaxValue)
}