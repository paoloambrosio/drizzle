package utils

import java.net.InetAddress

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * Trait that provides a WireMock server for HTTP-based tests.
  *
  * It will automatically start or reset the server before each
  * test and stop it at the end.
  */
trait WireMockSugar extends BeforeAndAfterAll with BeforeAndAfterEach { this: Suite =>

  protected def mockServerDefaultPort = 10000
  protected def mockServerMaxStartTries = 10

  protected val mockServerHost = InetAddress.getLocalHost.getHostAddress
  protected lazy val mockServerPort = wireMockServer.port()

  private lazy val wireMockServer = {
    @tailrec
    def startServerOnFirstAvailablePort(port: Int): WireMockServer = startServer(port) match {
      case Success(server) =>
        server
      case Failure(t) =>
        if (port >= port + mockServerMaxStartTries) throw t
        startServerOnFirstAvailablePort(port + 1)
    }
    startServerOnFirstAvailablePort(mockServerDefaultPort)
  }

  private def startServer(port: Int): Try[WireMockServer] = Try {
    val server = new WireMockServer(wireMockConfig().port(port))
    server.start()
    server
  }

  override protected def beforeEach {
    super.beforeEach
    WireMock.configureFor(mockServerHost, mockServerPort)
  }

  override protected def afterEach {
    super.afterEach
    WireMock.reset()
  }

  override protected def afterAll {
    super.afterAll
    wireMockServer.stop()
  }
}