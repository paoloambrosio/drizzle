import sbt._

object Resolvers {
  val dockerTestKitResolvers = Seq(
    "whisk" at "https://dl.bintray.com/whisk/maven",
    "softprops" at "http://dl.bintray.com/content/softprops/maven"
  )
}
