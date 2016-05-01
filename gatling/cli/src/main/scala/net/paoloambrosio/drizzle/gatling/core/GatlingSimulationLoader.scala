package net.paoloambrosio.drizzle.gatling.core

import java.time.Duration

import net.paoloambrosio.drizzle.cli.SimulationLoader
import net.paoloambrosio.drizzle.core.action.CoreActionFactory
import net.paoloambrosio.drizzle.core.{LoadInjectionStepsFactory, ScenarioProfile, ScenarioStep, Scenario => DrizzleScenario, Simulation => DrizzleSimulation}
import net.paoloambrosio.drizzle.gatling.core.{Scenario => GatlingScenario, Simulation => GatlingSimulation}
import net.paoloambrosio.drizzle.gatling.http.HttpRequest
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.concurrent.Future

trait GatlingSimulationLoader extends SimulationLoader with LoadInjectionStepsFactory { this: CoreActionFactory =>

  def load(ref: String): DrizzleSimulation = {
    val simClass = Class.forName(ref)
    val sim = simClass.newInstance().asInstanceOf[GatlingSimulation]
    toDrizzle(sim)
  }

  def toDrizzle(gaSim: GatlingSimulation): DrizzleSimulation = {
    new DrizzleSimulation {
      override def scenarioProfiles: Seq[ScenarioProfile] = {
        val setUp = gaSim.build
        println(setUp)
        setUp.populations.map(toDrizzle(_, setUp.protocols))
      }
    }
  }

  def toDrizzle(population: Population, protocols: Seq[Protocol]): ScenarioProfile = ScenarioProfile(
    scenario = toDrizzle(population.scenario, population.protocols ++ protocols),
    loadProfile = toDrizzle(population.injectionSteps)
  )

  def toDrizzle(scenario: GatlingScenario, protocols: Seq[Protocol]): DrizzleScenario = DrizzleScenario(
    name = scenario.name,
    steps = scenario.actions.toStream.flatMap(toDrizzle(_, protocols))
  )

  def toDrizzle(injectionSteps: Seq[InjectionStep]): Seq[Duration] = injectionSteps flatMap {
    case AtOnceInjection(users: Int) => verticalRamp(users)
    case _ => ???
  }

  def toDrizzle(action: Action, protocols: Seq[Protocol]): Stream[ScenarioStep] = action match {
    case PauseAction(duration) => Stream(ScenarioStep(None, thinkTime(duration)))
    case HttpRequest(name, verb, path) => Stream(ScenarioStep(Some(name), timedAction(vars => {
      println(s"HTTP -> $action $protocols")
      Future.successful(vars)
    })))
    case _ => ???
  }

}
