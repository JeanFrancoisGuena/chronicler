package com.fsanaulla.integration

import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.time.{Second, Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}

class UserManagementSpec
  extends FlatSpec
    with Matchers
    with DockerTestKit
    with DockerKitSpotify
    with DockerInfluxService {

  implicit val pc = PatienceConfig(Span(20, Seconds), Span(1, Second))

  "User management operation" should "correctly work" in {
//
//    lazy val host = influxdbContainer.getIpAddresses().futureValue
//    lazy val port = influxdbContainer.getPorts().futureValue.get(8086)
//
//    // CHECKING CONTAINER
//    isContainerReady(influxdbContainer).futureValue shouldBe true
//    port should not be None
//    host should not be Seq.empty
//
//    // INIT INFLUX CLIENT
//    val influx = new InfluxClient(host.head, 8086)
  }
}