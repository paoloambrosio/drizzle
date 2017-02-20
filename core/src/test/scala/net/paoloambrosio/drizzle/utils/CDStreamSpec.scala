package net.paoloambrosio.drizzle.utils

import org.scalatest._

class CDStreamSpec extends FlatSpec with Matchers {

  import CDStream._

  val anyContext = 42

  it can "be built statically" in {
    val cds = static[Int, String]("1", "2")

    cds(anyContext).head shouldBe "1"
    cds(anyContext).tail(anyContext).head shouldBe "2"
  }

  it can "be built by concatenation" in {
    val cds = "1" @:: "2" @:: CDStream.empty[Int, String] // CDStream needed because empty is a ScalaTest matcher

    cds(anyContext).head shouldBe "1"
    cds(anyContext).tail(anyContext).head shouldBe "2"
  }

  it should "match with implicit context" in {
    implicit val c = anyContext

    static[Int, String]("1", "2") match {
      case hd1 @:: hd2 @:: rest =>
        hd1 shouldBe "1"
        hd2 shouldBe "2"
      case _ => fail("did not match")
    }
  }

  "loop" should "loop on body if check passed" in {
    val cds = loop(
      (c: Int) => c == 0,
      "1" @:: "2" @:: CDStream.empty[Int, String]
    ) @::: "3" @:: CDStream.empty[Int, String]

    evaluate(cds, Seq(0, 0, 0, 1, 1)) shouldBe Seq("1", "2", "1", "2", "3")
  }

  "doIf" should "execute body only if check passed" in {
    val cds = loop(
      (c: Int) => c == 0,
      "1" @:: "2" @:: CDStream.empty[Int, String]
    ) @::: "3" @:: CDStream.empty[Int, String]

    evaluate(cds, Seq(1)) shouldBe Seq("3")
    evaluate(cds, Seq(0, 1, 1)) shouldBe Seq("1", "2", "3")
  }

  "map" should "transform content" in {
    val cds = loop(
      (c: Int) => c == 0,
      1 @:: 2 @:: CDStream.empty[Int, Int]
    ) @::: 3 @:: CDStream.empty[Int, Int]

    evaluate(cds.map(_.toString), Seq(0, 1, 1)) shouldBe Seq("1", "2", "3")
  }

  // during uses counterName to store when the loop started

  private def evaluate[C, T](s: CDStream[C, T], c: Seq[C]): Seq[T] = {
    c match {
      case chd :: ctl =>
        implicit val current: C = chd
        s match {
          case shd @:: stl =>
            shd +: evaluate(stl, ctl)
          case _ => Seq.empty
        }
      case _ => Seq.empty // it should verify that the stream is empty
    }
  }

}
