import sbt._

object Dependencies {
  private val akkaV = "2.5.12"
  private val dockerTestKitV = "0.9.6"

  val akkaActor              = "com.typesafe.akka"     %% "akka-actor"                  % akkaV
  val asyncHttpClient        = "org.asynchttpclient"    % "async-http-client"           % "2.4.7"
  val influxdbClient         = "com.paulgoldbaum"      %% "scala-influxdb-client"       % "0.6.0"
  val joddLagarto            = "org.jodd"               % "jodd-lagarto"                % "4.3.2"
  val scalaCsv               = "com.github.tototoshi"  %% "scala-csv"                   % "1.3.5"

  val slf4j                  = "org.slf4j"              % "slf4j-simple"                % "1.7.22"

  val akkaTestkit            = "com.typesafe.akka"     %% "akka-testkit"                % akkaV
  val akkaMockScheduler      = "com.miguno.akka"       %% "akka-mock-scheduler"         % "0.5.1"
  val dockerTestKitScalaTest = "com.whisk"             %% "docker-testkit-scalatest"    % dockerTestKitV
  val dockerTestKitSpotify   = "com.whisk"             %% "docker-testkit-impl-spotify" % dockerTestKitV
  val mockito                = "org.mockito"            % "mockito-core"                % "2.18.3"
  val scalaTest              = "org.scalatest"         %% "scalatest"                   % "3.0.5"
  val wiremock               = "com.github.tomakehurst" % "wiremock"                    % "2.17.0"
}
