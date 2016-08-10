package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.feeder.Feeder
import net.paoloambrosio.drizzle.feeder.csv.CsvFeederFactory

import scala.io.Source
import scala.util.Random

package object feeders {

  trait StandardFeeders {

    def csv(fileName: String): RichFeeder = {
      CsvFeederFactory.csv(Source.fromFile(resolve(fileName)))
    }

    private def resolve(fileName: String) = { // TODO
      val cl = getClass.getClassLoader
      cl.getResource(s"data/${fileName}").getFile
    }
  }

  implicit class RichFeeder(val self: Feeder) extends AnyVal {
    def random: Feeder = {
      val randomisedStream = Random.shuffle(self.toStream)
      Stream.continually(randomisedStream).flatten.toIterator
    }
  }

}
