package net.paoloambrosio.drizzle.utils

import org.scalatest._

class CDStreamSpec extends FlatSpec with Matchers {

  import CDStream._

  val f: Int => Int => String = x => c => (c + x).toString
  def constant[A](a: A): Int => A = _ => a

  it can "be built statically" in {
    val cds = static[Int, String](f(1), f(2))

    val entry = cds(10)
    entry.head shouldBe "11"
    entry.tail(20).head shouldBe "22"
  }

  it can "be built by concatenation" in {
    val cds = f(1) @:: f(2) @:: CDStream.empty[Int, String] // CDStream needed because empty is a ScalaTest matcher

    val entry = cds(10)
    entry.head shouldBe "11"
    entry.tail(20).head shouldBe "22"
  }

  it should "match with implicit context" in {
    implicit val c = 10

    static[Int, String](f(1), f(2)) match {
      case hd1 @:: hd2 @:: rest =>
        hd1 shouldBe "11"
        hd2 shouldBe "12"
      case _ => fail("did not match")
    }
  }

  "loop" should "execute the body if check is passed" in {
    val cds = loop(
      (c: Int) => (c, c == 0)
    )(
      f(1) @:: f(2) @:: CDStream.empty[Int, String]
    ) @::: f(3) @:: CDStream.empty[Int, String]

    evaluate(cds, Seq(0, 0, 0, 1, 1)) shouldBe Seq("1", "2", "1", "3", "4")
  }

  it should "propagate context changes in the check" in {
    val cds = loop(
      (c: Int) => (c - 3, c < 5)
    )(
      f(10) @:: f(20) @:: CDStream.empty[Int, String]
    ) @::: f(30) @:: CDStream.empty[Int, String]

    evaluate(cds, Seq(3, 3, 7)) shouldBe Seq("10", "23", "34")
  }

  "conditional" should "execute body only if check passed" in {
    val cds = loop(
      (c: Int) => (c, c == 0)
    )(
      constant("1") @:: constant("2") @:: CDStream.empty[Int, String]
    ) @::: constant("3") @:: CDStream.empty[Int, String]

    evaluate(cds, Seq(1)) shouldBe Seq("3")
    evaluate(cds, Seq(0, 1, 1)) shouldBe Seq("1", "2", "3")
  }

  "map" should "transform content" in {
    val cds = loop(
      (c: Int) => (c, c == 0)
    )(
      constant(1) @:: constant(2) @:: CDStream.empty[Int, Int]
    ) @::: constant(3) @:: CDStream.empty[Int, Int]

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
