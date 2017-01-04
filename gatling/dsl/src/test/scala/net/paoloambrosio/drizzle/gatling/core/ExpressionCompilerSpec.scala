package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.core.ScenarioContext
import net.paoloambrosio.drizzle.gatling.core.expression.{ExpressionCompiler, ExpressionCompilerException}
import org.scalatest.{FlatSpecLike, Matchers, TryValues}

import scala.util.Success

class ExpressionCompilerSpec extends FlatSpecLike with Matchers with TryValues {

  import net.paoloambrosio.drizzle.gatling.core.expression.ExpressionSupport._
  import ExpressionCompiler._

  val testScenario = ScenarioContext(sessionVariables = Map(
    "s1" -> "1",
    "i2" -> 2,
    "sC" -> "C"
  ))

  "extractParts" should "be empty for empty string" in {
    extractParts("") shouldBe Seq.empty
  }

  it should "extract a single static part" in {
    extractParts("not a variable") shouldBe Seq(StaticPart("not a variable"))
  }

  it should "extract a single dynamic part" in {
    extractParts("${variable name}") shouldBe Seq(DynamicPart("variable name"))
  }

  it should "extract dynamic part prefixed by static part" in {
    extractParts("some text${variable name}") shouldBe Seq(
      StaticPart("some text"),
      DynamicPart("variable name")
    )
  }

  it should "extract static part prefixed by dynamic part" in {
    extractParts("${variable name}some text") shouldBe Seq(
      DynamicPart("variable name"),
      StaticPart("some text")
    )
  }

  it should "extract dynamic part between static parts" in {
    extractParts("some text ${variable name} some other text") shouldBe Seq(
      StaticPart("some text "),
      DynamicPart("variable name"),
      StaticPart(" some other text")
    )
  }

  it should "extract multiple mixed dynamic and static parts" in {
    extractParts("a${b}c${d}${e}f") shouldBe Seq(
      StaticPart("a"),
      DynamicPart("b"),
      StaticPart("c"),
      DynamicPart("d"),
      DynamicPart("e"),
      StaticPart("f")
    )
  }

  "compile" should "leave untouched an empty string expression" in {
    val empty = "".el[String]
    empty(testScenario) shouldBe Success("")
  }

  it should "leave untouched static string expressions" in {
    val e = "constant".el[String]
    e(testScenario) shouldBe Success("constant")
  }

  it should "fail at compile time for static non-string expressions" in {
    an [ExpressionCompilerException] should be thrownBy "constant".el[Int]
  }

  it should "fail if value variable is not found" in {
    val e = "${nothing}".el[Any]
    e(testScenario).failure.exception should have message "'nothing' variable not found"
  }

  it should "return the variable if found" in {
    val se = "${s1}".el[String]
    se(testScenario) shouldBe Success("1")

    val ie = "${i2}".el[Int]
    ie(testScenario) shouldBe Success(2)
  }

  it should "fail if the variable cannot be casted" in {
    val e = "${sC}".el[Int]
    e(testScenario).failure.exception should have message "cannot cast type"
  }

  it should "convert variables in a string" in {
    val se = "X${s1}X${sC}X".el[String]
    se(testScenario) shouldBe Success("X1XCX")
  }
}

