import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Seq(
    organization := "net.paoloambrosio.drizzle",
    version      := "0.1",
    scalaVersion := "2.11.8"
  )
}

object Dependencies {
  val akkaV = "2.4.3"

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
    ).aggregate(core, metricsCommon, metricsInfluxDb)

  lazy val core =  Project(
      id = "core",
      base = file("core"),
      settings = commonSettings ++ Seq(
        libraryDependencies ++= Seq(
          akkaMockScheduler
        )
      )
    ).dependsOn(metricsCommon)

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

}

