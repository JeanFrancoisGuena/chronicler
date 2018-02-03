package com.github.fsanaulla.unit

import akka.http.scaladsl.model.Uri
import com.github.fsanaulla.TestSpec
import com.github.fsanaulla.core.query.ShardManagementQuery
import com.github.fsanaulla.handlers.AkkaQueryHandler
import com.github.fsanaulla.utils.TestHelper._

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 19.08.17
  */
class ShardManagementQuerySpec
  extends TestSpec
    with AkkaQueryHandler
    with ShardManagementQuery[Uri] {

  "drop shard by id" should "correctly work" in {
    dropShardQuery(5) shouldEqual queryTesterAuth("DROP SHARD 5")

    dropShardQuery(5)(emptyCredentials) shouldEqual queryTester("DROP SHARD 5")
  }

  "show shards" should "correctly work" in {
    showShards() shouldEqual queryTesterAuth("SHOW SHARDS")

    showShards()(emptyCredentials) shouldEqual queryTester("SHOW SHARDS")
  }

  "show shard groups" should "correctly work" in {
    showShardGroups() shouldEqual queryTesterAuth("SHOW SHARD GROUPS")

    showShardGroups()(emptyCredentials) shouldEqual queryTester("SHOW SHARD GROUPS")
  }
}
