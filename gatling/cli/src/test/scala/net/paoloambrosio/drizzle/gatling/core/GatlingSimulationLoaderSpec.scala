package net.paoloambrosio.drizzle.gatling.core

import java.net.{MalformedURLException, URL}
import java.time.Duration

import net.paoloambrosio.drizzle.core.action.CoreActionFactory
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.TimedPart
import net.paoloambrosio.drizzle.feeder._
import net.paoloambrosio.drizzle.http.HttpActionFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class GatlingSimulationLoaderSpec extends FlatSpec with Matchers {

  case class P1(i: Int) extends Protocol
  case class P2(s: String) extends Protocol

  "extract" should "find the first protocol of a type" in new TestContext {
    extract[P1](Seq(P2("x"),P1(4),P1(2),P2("y"))) shouldBe P1(4)
  }

  it should "error if it cannot find any" in new TestContext {
    an [NoSuchElementException] should be thrownBy extract[P1](Seq(P2("x"),P2("y")))
  }

  "fullURL" should "return the path if full URL" in new TestContext {
    fullURL(Seq.empty, "http://example.com/") shouldBe new URL("http://example.com/")
  }

  it should "use the base URL if relative URL is passed" in new TestContext {
    fullURL(Seq(new URL("http://example.com/a/b")), "c") shouldBe new URL("http://example.com/a/c")
    fullURL(Seq(new URL("http://example.com/a/b")), "/c") shouldBe new URL("http://example.com/c")
  }

  it should "error on relative URL without base URLs" in new TestContext {
    a [MalformedURLException] should be thrownBy fullURL(Seq.empty, "/")
  }

  trait TestContext extends GatlingSimulationLoader
      with CoreActionFactory with HttpActionFactory with FeederActionFactory {
    implicit def ec: ExecutionContext = ???
    override def timedAction[T](f: TimedPart[T]) = ???
    override def pacing(duration: Duration) = ???
    override def thinkTime(duration: Duration) = ???
    override def feed(feeder: Feeder) = ???
    def httpGet(url: URL) = ???
    def httpPost(url: URL) = ???
  }
}
