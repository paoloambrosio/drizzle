package net.paoloambrosio.drizzle.gatling.core

import java.net.{MalformedURLException, URL}
import java.time.Duration

import net.paoloambrosio.drizzle.core.{ActionTimers, ScenarioAction}
import net.paoloambrosio.drizzle.core.action.CoreActionFactory
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.{PostTimedPart, PreTimedPart, TimedPart}
import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.feeder._
import net.paoloambrosio.drizzle.http.HttpRequest
import net.paoloambrosio.drizzle.http.action.HttpActionFactory
import net.paoloambrosio.drizzle.http.checks.HttpCheck
import org.scalatest.{FlatSpec, Matchers, TryValues}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class GatlingSimulationLoaderSpec extends FlatSpec with Matchers with TryValues {

  case class P1(i: Int) extends Protocol
  case class P2(s: String) extends Protocol

  "extract" should "find the first protocol of a type" in new TestContext {
    extract[P1](Seq(P2("x"),P1(4),P1(2),P2("y"))) shouldBe P1(4)
  }

  it should "error if it cannot find any" in new TestContext {
    an [NoSuchElementException] should be thrownBy extract[P1](Seq(P2("x"),P2("y")))
  }

  "fullURL" should "return the path if full URL" in new TestContext {
    fullURL(Seq.empty)("http://example.com/") shouldBe Success("http://example.com/")
  }

  it should "use the base URL if relative URL is passed" in new TestContext {
    fullURL(Seq(new URL("http://example.com/a/b")))("c") shouldBe Success("http://example.com/a/c")
    fullURL(Seq(new URL("http://example.com/a/b")))("/c") shouldBe Success("http://example.com/c")
  }

  it should "error on relative URL without base URLs" in new TestContext {
    fullURL(Seq.empty)("/").failure.exception should be (a[MalformedURLException])
  }

  trait TestContext extends GatlingSimulationLoader
      with CoreActionFactory with HttpActionFactory with FeederActionFactory {
    implicit def ec: ExecutionContext = ???
    override def pacing(duration: Duration) = ???
    override def thinkTime(duration: Duration) = ???
    override def feed(feeder: Feeder) = ???
    override def httpRequest(request: Expression[HttpRequest], checks: Seq[HttpCheck]): ScenarioAction = ???
    override def timed[I, O](f: TimedPart[I, O]): TimedPart[I, (ActionTimers, O)] = ???
  }
}
