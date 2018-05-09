import sbt._

object Dependencies {
  private val akkaV = "2.4.16"

  val akkaActor              = "com.typesafe.akka"     %% "akka-actor"                  % "2.4.16"
  val asyncHttpClient        = "org.asynchttpclient"    % "async-http-client"           % "2.0.24"
  val influxdbClient         = "com.paulgoldbaum"      %% "scala-influxdb-client"       % "0.5.2"
  val joddLagarto            = "org.jodd"               % "jodd-lagarto"                % "3.8.1"
  val scalaCsv               = "com.github.tototoshi"  %% "scala-csv"                   % "1.3.4"

  val slf4j                  = "org.slf4j"              % "slf4j-simple"                % "1.7.22"

  val akkaTestkit            = "com.typesafe.akka"     %% "akka-testkit"                % "2.4.16"
  val akkaMockScheduler      = "com.miguno.akka"       %% "akka-mock-scheduler"         % "0.5.0"
  val dockerTestKitScalaTest = "com.whisk"             %% "docker-testkit-scalatest"    % "0.9.0-RC2"
  val dockerTestKitSpotify   = "com.whisk"             %% "docker-testkit-impl-spotify" % "0.9.0-RC2"
  val mockito                = "org.mockito"            % "mockito-core"                % "2.5.5"
  val scalaTest              = "org.scalatest"         %% "scalatest"                   % "3.0.1"
  val wiremock               = "com.github.tomakehurst" % "wiremock"                    % "2.5.0"
}
