package com.fsanaulla.integration

import akka.http.scaladsl.model.StatusCodes
import com.fsanaulla.InfluxClient
import com.fsanaulla.model.{UserInfo, UserPrivilegesInfo}
import com.fsanaulla.utils.constants.Privileges
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

    lazy val host = influxdbContainer.getIpAddresses().futureValue
    lazy val port = influxdbContainer.getPorts().futureValue.get(8086)

    // CHECKING CONTAINER
    isContainerReady(influxdbContainer).futureValue shouldBe true
    port should not be None
    host should not be Seq.empty

    // INIT INFLUX CLIENT
    val influx = new InfluxClient(host.head, 8086)

    influx.createDatabase("mydb").futureValue.status shouldEqual StatusCodes.OK

    influx.createUser("Martin", "password").futureValue.status shouldEqual StatusCodes.OK
    influx.showUsers.futureValue shouldEqual Seq(UserInfo("Martin", isAdmin = false))

    influx.createAdmin("Admin", "admin_pass")
    influx.showUsers.futureValue shouldEqual Seq(UserInfo("Martin", isAdmin = false), UserInfo("Admin", isAdmin = true))

    influx.showUserPrivileges("Admin").futureValue shouldEqual Nil

    influx.setUserPassword("Martin", "new_password").futureValue.status shouldEqual StatusCodes.OK

    influx.setPrivileges("Martin", "mydb", Privileges.READ).futureValue.status shouldEqual StatusCodes.OK
    influx.showUserPrivileges("Martin").futureValue shouldEqual Seq(UserPrivilegesInfo("mydb", "READ"))

    influx.revokePrivileges("Martin", "mydb", "READ").futureValue.status shouldEqual StatusCodes.OK
    influx.showUserPrivileges("Martin").futureValue shouldEqual Seq(UserPrivilegesInfo("mydb", "NO PRIVILEGES"))

    influx.disableAdmin("Admin").futureValue.status shouldEqual StatusCodes.OK
    influx.showUsers.futureValue shouldEqual Seq(UserInfo("Martin", isAdmin = false), UserInfo("Admin", isAdmin = false))

    influx.makeAdmin("Admin").futureValue.status shouldEqual StatusCodes.OK
    influx.showUsers.futureValue shouldEqual Seq(UserInfo("Martin", isAdmin = false), UserInfo("Admin", isAdmin = true))

    influx.dropUser("Martin").futureValue.status shouldEqual StatusCodes.OK
    influx.showUsers.futureValue shouldEqual Seq(UserInfo("Admin", isAdmin = true))

    influx.close
  }
}
