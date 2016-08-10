package net.paoloambrosio.drizzle.feeder.csv

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class CsvFeederSpec extends FlatSpec with Matchers {

  it should "read an empty CSV file as an empty iterator" in new CsvFeederFactory {
    val csvSource = Source.fromString("")

    csv(csvSource).toList shouldBe Nil
  }

  it should "read a CSV source with headers" in new CsvFeederFactory {
    val csvSource = Source.fromString(
      """|a,b
         |a1,b1
         |a2,b2
         |""".stripMargin)

    csv(csvSource).toList shouldBe List(
      Map("a" -> "a1", "b" -> "b1"),
      Map("a" -> "a2", "b" -> "b2")
    )
  }
}
