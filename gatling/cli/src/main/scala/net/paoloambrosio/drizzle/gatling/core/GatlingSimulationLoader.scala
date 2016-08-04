package net.paoloambrosio.drizzle.gatling.core

import java.net.URL
import java.time.Duration

import net.paoloambrosio.drizzle.cli.SimulationLoader
import net.paoloambrosio.drizzle.core.action.CoreActionFactory
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.TimedAction
import net.paoloambrosio.drizzle.core.{LoadInjectionStepsFactory, ScenarioProfile, ScenarioStep, Scenario => DrizzleScenario, Simulation => DrizzleSimulation}
import net.paoloambrosio.drizzle.gatling.core.{Scenario => GatlingScenario, Simulation => GatlingSimulation}
import net.paoloambrosio.drizzle.gatling.http.HttpRequest._
import net.paoloambrosio.drizzle.gatling.http.{HttpProtocol, HttpRequest => GatlingHttpRequest}
import net.paoloambrosio.drizzle.http.{HttpActionFactory, HttpResponse}
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
    case AtOnceInjection(users) => verticalRamp(users)
    case RampInjection(users, duration) => linearRamp(users, duration)
    case _ => ???
  }

  protected def toDrizzle(action: Action, protocols: Seq[Protocol]): Stream[ScenarioStep] = action match {
    case PauseAction(duration) => Stream(ScenarioStep(None, thinkTime(duration)))
    case r: GatlingHttpRequest => {
      val httpProtocol: HttpProtocol = extract[HttpProtocol](protocols)
      val fullUrl = fullURL(httpProtocol.baseURLs, r.path)
      val fullHeaders = httpProtocol.headers ++ r.headers
      val actionBuilder = httpAction(r.method, fullUrl).headers(fullHeaders.toSeq)
      val action: TimedAction[HttpResponse] = timedAction(if (!r.formParams.isEmpty) actionBuilder.entity(r.formParams) else actionBuilder)
      r.checks.foldLeft(action)((a,c) => a.andThen(_.map { case (s,r) => c(s,r) }))
      Stream(ScenarioStep(Some(r.name), action))
    }
    case _ => ???
  }

  def httpAction(method: HttpMethod, url: URL) = method match {
    case Get => httpGet(url)
    case Post => httpGet(url)
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
