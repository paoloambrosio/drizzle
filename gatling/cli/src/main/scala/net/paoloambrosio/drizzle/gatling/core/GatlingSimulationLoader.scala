package net.paoloambrosio.drizzle.gatling.core

import java.net.URL
import java.time.Duration

import net.paoloambrosio.drizzle.cli.SimulationLoader
import net.paoloambrosio.drizzle.core.action.CoreActionFactory
import net.paoloambrosio.drizzle.core.{LoadInjectionStepsFactory, ScenarioProfile, ScenarioStep, Scenario => DrizzleScenario, Simulation => DrizzleSimulation}
import net.paoloambrosio.drizzle.gatling.core.{Scenario => GatlingScenario, Simulation => GatlingSimulation}
import net.paoloambrosio.drizzle.gatling.http.{HttpProtocol, HttpRequest => GatlingHttpRequest}
import net.paoloambrosio.drizzle.http.model.{HttpRequest => DrizzleHttpRequest, NoEntity, FormUrlEncodedEntity}
import net.paoloambrosio.drizzle.http.action.HttpActionFactory
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.reflect.{ClassTag, classTag}

trait GatlingSimulationLoader extends SimulationLoader with LoadInjectionStepsFactory {
    this: CoreActionFactory with HttpActionFactory =>

  def load(ref: String): DrizzleSimulation = {
    val simClass = Class.forName(ref)
    val sim = simClass.newInstance().asInstanceOf[GatlingSimulation]
    toDrizzle(sim)
  }

  protected def toDrizzle(gaSim: GatlingSimulation): DrizzleSimulation = {
    new DrizzleSimulation {
      override def scenarioProfiles: Seq[ScenarioProfile] = {
        val setUp = gaSim.build
        setUp.populations.map(toDrizzle(_, setUp.protocols))
      }
    }
  }

  protected def toDrizzle(population: Population, protocols: Seq[Protocol]): ScenarioProfile = ScenarioProfile(
    scenario = toDrizzle(population.scenario, population.protocols ++ protocols),
    loadProfile = toDrizzle(population.injectionSteps)
  )

  protected def toDrizzle(scenario: GatlingScenario, protocols: Seq[Protocol]): DrizzleScenario = DrizzleScenario(
    name = scenario.name,
    steps = scenario.actions.toStream.flatMap(toDrizzle(_, protocols))
  )

  protected def toDrizzle(injectionSteps: Seq[InjectionStep]): Seq[Duration] = injectionSteps flatMap {
    case AtOnceInjection(users: Int) => verticalRamp(users)
    case _ => ???
  }

  protected def toDrizzle(action: Action, protocols: Seq[Protocol]): Stream[ScenarioStep] = action match {
    case PauseAction(duration) => Stream(ScenarioStep(None, thinkTime(duration)))
    case GatlingHttpRequest(name, verb, path, headers, formParams) => {
      val httpProtocol: HttpProtocol = extract[HttpProtocol](protocols)
      val fullUrl = fullURL(httpProtocol.baseURLs, path)
      val fullHeaders = httpProtocol.headers ++ headers
      val entity = if (formParams.isEmpty) NoEntity else FormUrlEncodedEntity(formParams)
      Stream(ScenarioStep(Some(name), httpAction(DrizzleHttpRequest(verb, fullUrl, fullHeaders.toSeq, entity))))
    }
    case _ => ???
  }

  protected def fullURL(baseUrls: Seq[URL], path: String): URL = baseUrls match {
    case Nil => new URL(path)
    case baseUrl :: Nil => new URL(baseUrl, path)
    case _ => throw new NotImplementedError("Multiple base URLs not supported yet")
  }

  protected def extract[T: ClassTag](protocols: Seq[Protocol]): T = {
    val clazz = classTag[T].runtimeClass
    protocols.find(clazz.isInstance(_)).asInstanceOf[Option[T]]
      .getOrElse(throw new NoSuchElementException(s"No protocol of type ${clazz.getSimpleName} found"))
  }
}
