import sbt.Keys._
import sbt._

object BuildSettings {
  val buildSettings = Seq(
    organization := "net.paoloambrosio.drizzle",
    version      := "0.1",
    scalaVersion := "2.11.8"
  )
}

object Dependencies {
  private val akkaV = "2.4.16"

  val akkaActor              = "com.typesafe.akka"     %% "akka-actor"                  % "2.4.16"
  val asyncHttpClient        = "org.asynchttpclient"    % "async-http-client"           % "2.0.24"
  val influxdbClient         = "com.paulgoldbaum"      %% "scala-influxdb-client"       % "0.5.2"
  val scalaCsv               = "com.github.tototoshi"  %% "scala-csv"                   % "1.3.4"
  val wiremock               = "com.github.tomakehurst" % "wiremock"                    % "2.5.0"

  val slf4j                  = "org.slf4j"              % "slf4j-simple"                % "1.7.22"

  val akkaTestkit            = "com.typesafe.akka"     %% "akka-testkit"                % "2.4.16"
  val akkaMockScheduler      = "com.miguno.akka"       %% "akka-mock-scheduler"         % "0.5.0"
  val dockerTestKitScalaTest = "com.whisk"             %% "docker-testkit-scalatest"    % "0.9.0-RC2"
  val dockerTestKitSpotify   = "com.whisk"             %% "docker-testkit-impl-spotify" % "0.9.0-RC2"
  val mockito                = "org.mockito"            % "mockito-core"                % "2.5.5"
  val scalaTest              = "org.scalatest"         %% "scalatest"                   % "3.0.1"
}

object Resolvers {
  val dockerTestKitResolvers = Seq(
    "whisk" at "https://dl.bintray.com/whisk/maven",
    "softprops" at "http://dl.bintray.com/content/softprops/maven"
  )
}

object DrizzleBuild extends Build {

  import BuildSettings._
  import Dependencies._
  import Resolvers._

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

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings
  ).aggregate(core, httpExtensions, standardExtensions, cli, metricsCommon, metricsInfluxDb, gatlingCli, gatlingDsl)

  lazy val core =  Project(
    id = "core",
    base = file("core"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        akkaMockScheduler % Test
      )
    )
  ).dependsOn(metricsCommon)

  lazy val httpExtensions =  Project(
    id = "http-extensions",
    base = file("extensions/http"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        asyncHttpClient,
        wiremock % Test
      )
    )
  ).dependsOn(core % "test->test;compile->compile")

  lazy val standardExtensions =  Project(
    id = "standard-extensions",
    base = file("extensions/standard"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        scalaCsv
      )
    )
  ).dependsOn(core)

  lazy val cli =  Project(
    id = "cli",
    base = file("cli"),
    settings = commonSettings
  ).dependsOn(core, httpExtensions)

  lazy val metricsCommon =  Project(
    id = "metrics-common",
    base = file("metrics/common"),
    settings = commonSettings
  )

  lazy val metricsInfluxDb = Project(
    id = "metrics-influxdb",
    base = file("metrics/influxdb"),
    settings = commonSettings ++ dockerTestKitSettings ++ Seq(
      libraryDependencies ++= Seq(
        influxdbClient
      )
    )
  ).dependsOn(metricsCommon)

  lazy val gatlingDsl =  Project(
    id = "gatling-dsl",
//    version = s"${version}-2.1.7", // TODO
    base = file("gatling/dsl"),
    settings = commonSettings
  ).dependsOn(httpExtensions, standardExtensions)

  lazy val gatlingCli =  Project(
    id = "gatling-cli",
    //    version = s"${version}-2.1.7", // TODO
    base = file("gatling/cli"),
    settings = commonSettings  ++ Seq(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      )
    )
  ).dependsOn(cli, gatlingDsl)

  // TODO: create a separate build for the examples!

  lazy val gatlingTutorial =  Project(
    id = "gatling-tutorial-example",
    base = file("examples/gatling-tutorial"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        slf4j % Runtime // Logging configuration should be user-defined
      )
    )
  ).dependsOn(gatlingCli)

}
