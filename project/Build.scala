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
  val akkaV = "2.4.4"

  val akkaActor              = "com.typesafe.akka" %% "akka-actor"                  % akkaV

  val scalaTest              = "org.scalatest"     %% "scalatest"                   % "2.2.6"        % Test
  val mockito                = "org.mockito"       %  "mockito-core"                % "1.10.19"      % Test
  val akkaTestkit            = "com.typesafe.akka" %% "akka-testkit"                % akkaV          % Test
  val akkaMockScheduler      = "com.miguno.akka"   %% "akka-mock-scheduler"         % "0.4.0"        % Test
  val dockerTestKitScalaTest = "com.whisk"         %% "docker-testkit-scalatest"    % "0.6.1"        % Test
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

  val commonDeps = Seq(
    akkaActor,
    akkaTestkit,
    scalaTest,
    mockito
  )

  val commonSettings = buildSettings ++ Seq(
    libraryDependencies ++= commonDeps
  )

  val dockerTestKitSettings = Seq(
    resolvers ++= dockerTestKitResolvers,
    libraryDependencies ++= Seq(
      dockerTestKitScalaTest
    )
  )

  lazy val root = Project(
      id = "root",
      base = file("."),
      settings = buildSettings
    ).aggregate(core, http, cli, metricsCommon, metricsInfluxDb, gatlingCli, gatlingDsl)

  lazy val core =  Project(
      id = "core",
      base = file("core"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(
          akkaMockScheduler
        )
      )
    ).dependsOn(metricsCommon)

  lazy val http =  Project(
    id = "http",
    base = file("http"),
    settings = commonSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http-core" % akkaV,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
        "org.asynchttpclient" % "async-http-client" % "2.0.2",
        "com.github.tomakehurst" % "wiremock" % "1.58" % Test
      )
    )
  ).dependsOn(core % "test->test;compile->compile")

  lazy val cli =  Project(
      id = "cli",
      base = file("cli"),
      settings = commonSettings
    ).dependsOn(core, http)

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
          "com.paulgoldbaum" %% "scala-influxdb-client" % "0.4.5"
        )
      )
    ).dependsOn(metricsCommon)

  lazy val gatlingDsl =  Project(
    id = "gatling-dsl",
//    version = s"${version}-2.1.7", // TODO
    base = file("gatling/dsl"),
    settings = buildSettings
  ).dependsOn(http)

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
