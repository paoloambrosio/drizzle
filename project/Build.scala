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
  private val akkaV = "2.4.8"

  val akkaActor              = "com.typesafe.akka"     %% "akka-actor"               % akkaV
  val akkaHttpCore           = "com.typesafe.akka"     %% "akka-http-core"           % akkaV
  val akkaHttpExperimental   = "com.typesafe.akka"     %% "akka-http-experimental"   % akkaV
  val asyncHttpClient        = "org.asynchttpclient"    % "async-http-client"        % "2.0.11"
  val influxdbClient         = "com.paulgoldbaum"      %% "scala-influxdb-client"    % "0.5.0"
  val scalaCsv               = "com.github.tototoshi"  %% "scala-csv"                % "1.3.3"
  val wiremock               = "com.github.tomakehurst" % "wiremock"                 % "2.1.9"

  val akkaTestkit            = "com.typesafe.akka"     %% "akka-testkit"             % akkaV
  val akkaMockScheduler      = "com.miguno.akka"       %% "akka-mock-scheduler"      % "0.4.0"
  val dockerTestKitScalaTest = "com.whisk"             %% "docker-testkit-scalatest" % "0.8.2"
  val mockito                = "org.mockito"           % "mockito-core"             % "1.10.19"
  val scalaTest              = "org.scalatest"         %% "scalatest"                % "3.0.0"
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
    )
  )

  val dockerTestKitSettings = Seq(
    resolvers ++= dockerTestKitResolvers,
    libraryDependencies ++= Seq(
      dockerTestKitScalaTest % Test
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
        akkaHttpCore,
        akkaHttpExperimental,
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
  ).dependsOn(httpExtensions)

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
    settings = buildSettings
  ).dependsOn(gatlingCli)

}
