package io.gatling.http

import net.paoloambrosio.drizzle.gatling.http.HttpSupport
import net.paoloambrosio.drizzle.gatling.http.checks.HttpChecks

/**
  * Gatling HTTP DSL
  */
object Predef extends HttpSupport with HttpChecks {

}