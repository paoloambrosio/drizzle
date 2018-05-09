package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.feeder.Feeder
import net.paoloambrosio.drizzle.feeder.csv.CsvFeederFactory

import scala.io.Source
import scala.util.Random

package object feeders {

  trait StandardFeeders {

    def csv(resourceName: String): RichFeeder = {
      CsvFeederFactory.csv(Source.fromResource(resolve(resourceName)))
    }

    private def resolve(fileName: String) = s"data/${fileName}" // TODO
  }

  implicit class RichFeeder(val self: Feeder) extends AnyVal {
    def random: Feeder = {
      val randomisedStream = Random.shuffle(self.toStream)
      Stream.continually(randomisedStream).flatten.toIterator
    }
  }

}
