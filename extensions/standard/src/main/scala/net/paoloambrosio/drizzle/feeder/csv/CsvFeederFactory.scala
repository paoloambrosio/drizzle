package net.paoloambrosio.drizzle.feeder.csv

import com.github.tototoshi.csv.CSVReader
import net.paoloambrosio.drizzle.feeder.Feeder

import scala.io.Source

object CsvFeederFactory {

  def csv(source: Source): Feeder = CSVReader.open(source).iteratorWithHeaders

}
