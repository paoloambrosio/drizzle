import Dependencies._
import Resolvers._

val buildSettings = Seq(
  organization := "net.paoloambrosio.drizzle",
  version      := "0.1",
  scalaVersion := "2.12.6"
)

val commonSettings = buildSettings ++ Seq(
  libraryDependencies ++= Seq(
    akkaActor,
    akkaTestkit % Test,
    scalaTest % Test,
    mockito % Test
  ),
  fork in Test := true
)

val dockerTestKitSettings = Seq(
  resolvers ++= dockerTestKitResolvers,
  libraryDependencies ++= Seq(
    dockerTestKitScalaTest % Test,
    dockerTestKitSpotify % Test
  )
)

lazy val root = (project in file("."))
  .settings(buildSettings)
  .aggregate(core, httpExtensions, standardExtensions, cli, metricsCommon, metricsInfluxDb, gatlingCli, gatlingDsl)

lazy val core =  (project in file("core"))
  .settings(commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      akkaMockScheduler % Test
    )
  )).dependsOn(metricsCommon)

lazy val httpExtensions =  (project in file("extensions/http"))
  .settings(commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      asyncHttpClient,
      wiremock % Test
    )
  )).dependsOn(core % "test->test;compile->compile")

lazy val standardExtensions =  (project in file("extensions/standard"))
  .settings(commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      scalaCsv
    )
  )).dependsOn(core)

lazy val cli =  (project in file("cli"))
  .settings(commonSettings)
  .dependsOn(core, httpExtensions)

lazy val metricsCommon =  (project in file("metrics/common"))
  .settings(commonSettings)

lazy val metricsInfluxDb = (project in file("metrics/influxdb"))
  .settings(commonSettings ++ dockerTestKitSettings ++ Seq(
    libraryDependencies ++= Seq(
      influxdbClient
    )
  )).dependsOn(metricsCommon)

lazy val gatlingDsl =  (project in file("gatling/dsl")) // TODO override with = s"${drizzleVersion}-${gatlingVersion}"
  .settings(commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      joddLagarto
    )
  )).dependsOn(httpExtensions, standardExtensions)

lazy val gatlingCli =  (project in file("gatling/cli")) // TODO override with = s"${drizzleVersion}-${gatlingVersion}"
  .settings(commonSettings ++ Seq(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )).dependsOn(cli, gatlingDsl)

// TODO: create a separate build for the examples!

lazy val gatlingTutorial =  (project in file("examples/gatling-tutorial"))
  .settings(buildSettings ++ Seq(
    libraryDependencies ++= Seq(
      slf4j % Runtime // Logging configuration should be user-defined
    )
  )).dependsOn(gatlingCli)