package utils

import java.net.InetAddress

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

/**
  * Trait that provides a WireMock server for HTTP-based tests.
  *
  * It will automatically start or reset the server before each
  * test and stop it at the end.
  */
trait WireMockSugar extends BeforeAndAfterAll with BeforeAndAfterEach { this: Suite =>

  protected val mockServerHost = InetAddress.getLocalHost.getHostAddress
  protected val mockServerPort = 10000

  private val wireMockServer = new WireMockServer(wireMockConfig().port(mockServerPort))

  override protected def beforeAll {
    super.beforeAll
    wireMockServer.start()
    WireMock.configureFor(mockServerHost, mockServerPort)
  }

  override protected def beforeEach {
    super.beforeEach
    WireMock.reset()
  }

  override protected def afterAll {
    super.afterAll
    wireMockServer.stop()
  }
}