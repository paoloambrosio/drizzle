package net.paoloambrosio.drizzle.gatling.core

import java.net.URL
import java.time.Duration

import net.paoloambrosio.drizzle.cli.SimulationLoader
import net.paoloambrosio.drizzle.core.action.CoreActionFactory
import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.core.{LoadInjectionStepsFactory, ScenarioAction, ScenarioProfile, ScenarioStep, StepStream, Scenario => DrizzleScenario, Simulation => DrizzleSimulation}
import net.paoloambrosio.drizzle.feeder.FeederActionFactory
import net.paoloambrosio.drizzle.gatling.core.{Scenario => GatlingScenario, Simulation => GatlingSimulation}
import net.paoloambrosio.drizzle.gatling.http.{HttpAction, HttpProtocol}
import net.paoloambrosio.drizzle.http.HttpRequest
import net.paoloambrosio.drizzle.http.action.HttpActionFactory
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.reflect.{ClassTag, classTag}
import scala.util.Try

trait GatlingSimulationLoader extends SimulationLoader with LoadInjectionStepsFactory
    with FeederActionFactory {
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
    steps = toDrizzle(scenario.actions, protocols)
  )

  protected def toDrizzle(actions: Seq[GatlingAction], protocols: Seq[Protocol]): StepStream = {
    // TODO handle dynamic ones as well
    StepStream.static(actions.flatMap(toDrizzle(_, protocols)))
  }

  protected def toDrizzle(injectionSteps: Seq[InjectionStep]): Seq[Duration] = injectionSteps flatMap {
    case AtOnceInjection(users) => verticalRamp(users)
    case RampInjection(users, duration) => linearRamp(users, duration)
    case _ => ???
  }

  protected def toDrizzle(action: GatlingAction, protocols: Seq[Protocol]): Stream[ScenarioStep] = action match {
    case PauseAction(duration) => Stream(ScenarioStep(None, thinkTime(duration)))
    case FeedingAction(feeder) => Stream(ScenarioStep(None, feed(feeder)))
    case HttpAction(hrb) => {
      val requestEx = applyProtocol(hrb.requestEx, extract[HttpProtocol](protocols))
      val action = httpRequest(requestEx, hrb.checks)
      Stream(ScenarioStep(Some(hrb.stepNameEx), action))
    }
    case _ => ???
  }

  def applyProtocol(requestEx: Expression[HttpRequest], httpProtocol: HttpProtocol): Expression[HttpRequest] = {
    transformUri(requestEx, fullURL(httpProtocol.baseURLs))
  }

  //    val fullHeaders = httpProtocol.headers ++ r.headers
  //    val actionBuilder = httpAction(r.method, fullUrl).headers(fullHeaders.toSeq)
  //    timedAction(if (!r.formParams.isEmpty) actionBuilder.entity(r.formParams) else actionBuilder)

  private def transformUri(requestEx: Expression[HttpRequest], transform: String => Try[String]): Expression[HttpRequest] =
    requestEx andThen { for {
      request <- _
      newUri <- transform(request.uri)
    } yield request.copy(uri = newUri)
  }

  protected def fullURL(baseUrls: Seq[URL])(uri: String): Try[String] = Try { baseUrls match {
    case Nil => new URL(uri).toString
    case baseUrl :: Nil => new URL(baseUrl, uri).toString
    case _ => throw new NotImplementedError("Multiple base URLs not supported yet")
  }}

  protected def extract[T: ClassTag](protocols: Seq[Protocol]): T = {
    val clazz = classTag[T].runtimeClass
    protocols.find(clazz.isInstance(_)).asInstanceOf[Option[T]]
      .getOrElse(throw new NoSuchElementException(s"No protocol of type ${clazz.getSimpleName} found"))
  }
}
