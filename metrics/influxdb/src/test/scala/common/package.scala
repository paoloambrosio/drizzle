import scala.util.Random

package object common {

  private lazy val random = new Random()

  def randomAlphaNumeric(length: Int) = (random.alphanumeric take length).mkString
}
