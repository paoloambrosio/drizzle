package net.paoloambrosio.drizzle.gatling.core

import java.net.URL
import java.time.Duration

import net.paoloambrosio.drizzle.cli.SimulationLoader
import net.paoloambrosio.drizzle.core.action.CoreActionFactory
import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.core.{ActionResult, ActionStep, ConditionalStep, LoadInjectionStepsFactory, LoopStep, ScenarioAction, ScenarioContext, ScenarioProfile, ScenarioStep, SessionVariables, Scenario => DrizzleScenario, Simulation => DrizzleSimulation}
import net.paoloambrosio.drizzle.feeder.FeederActionFactory
import net.paoloambrosio.drizzle.gatling.core.{Scenario => GatlingScenario, Simulation => GatlingSimulation}
import net.paoloambrosio.drizzle.gatling.http.{HttpAction, HttpProtocol}
import net.paoloambrosio.drizzle.http.HttpRequest
import net.paoloambrosio.drizzle.http.action.HttpActionFactory
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.concurrent.Future
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

  protected def toDrizzle(actions: Seq[GatlingAction], protocols: Seq[Protocol]): Seq[ScenarioStep] = {
    actions.map(toDrizzle(_, protocols))
  }

  protected def toDrizzle(injectionSteps: Seq[InjectionStep]): Seq[Duration] = injectionSteps flatMap {
    case AtOnceInjection(users) => verticalRamp(users)
    case RampInjection(users, duration) => linearRamp(users, duration)
    case _ => ???
  }

  protected def toDrizzle(action: GatlingAction, protocols: Seq[Protocol]): ScenarioStep = action match {
    case PauseAction(duration) => ActionStep(None, thinkTime(duration))
    case FeedingAction(feeder) => ActionStep(None, feed(feeder))
    case HttpAction(hrb) => {
      val requestEx = applyProtocol(hrb.requestEx, extract[HttpProtocol](protocols))
      val action = httpRequest(requestEx, hrb.checks)
      ActionStep(Some(hrb.stepNameEx), action)
    }
    case LoopAction(times, counterName, body) => LoopStep(c => {
      val sv = c.sessionVariables
      val counter = incrementCounter(sv, counterName)
      val exit = counter > times(c).get
      val newSv = if (exit) sv - (counterName) else sv + ((counterName, counter))
      (c.copy(sessionVariables = newSv), !exit)
    }, body.map(toDrizzle(_, protocols)))
    case TryAction(times, counterName, body) => LoopStep(c => {
      // TODO refactor: exit condition is the only difference with LoopAction
      val sv = c.sessionVariables
      val counter = incrementCounter(sv, counterName)
      // NOTE: we check for errors only after executing the block at least once
      val exit = (counter > times(c).get) || (counter > 1 && c.latestAction.error.isEmpty)
      val newSv = if (exit) sv - (counterName) else sv + ((counterName, counter))
      (c.copy(sessionVariables = newSv), !exit)
    }, stopAtFirstFailure(body, protocols))
    case ExitOnErrorAction => ActionStep(None, scenarioFailureToFutureFailure)
    case _ => ???
  }

  val scenarioFailureToFutureFailure: ScenarioAction = _ match {
    case ScenarioContext(ActionResult(_, Some(exception)), _) => Future.failed(exception)
    case sc => Future.successful(sc)
  }

  // TODO refactor
  def stopAtFirstFailure(gas: List[GatlingAction], protocols: Seq[Protocol]): List[ScenarioStep] = gas match {
    case ga1 :: ga2 :: tl => List(toDrizzle(ga1, protocols), ConditionalStep(sc => sc.latestAction.error.isEmpty, stopAtFirstFailure(ga2 :: tl, protocols)))
    case ga :: Nil => List(toDrizzle(ga, protocols))
    case Nil => Nil
  }

  def incrementCounter(sv: SessionVariables, counterName: String) = {
    sv.get(counterName).map(_.asInstanceOf[Int] + 1).getOrElse(1)
  }

  def applyProtocol(requestEx: Expression[HttpRequest], httpProtocol: HttpProtocol): Expression[HttpRequest] = {
    transformUri(requestEx, fullURL(httpProtocol.baseURLs))
  }

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
