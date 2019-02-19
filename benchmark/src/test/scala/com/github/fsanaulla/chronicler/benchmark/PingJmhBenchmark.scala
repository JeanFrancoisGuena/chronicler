package com.github.fsanaulla.chronicler.benchmark

import java.util.concurrent.TimeUnit

import com.github.fsanaulla.chronicler.ahc.io.{AhcIOClient, InfluxIO}
import org.openjdk.jmh.annotations._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class PingJmhBenchmark {
  import PingJmhBenchmark._
  @Benchmark
  def averagePingTime(state: BenchmarkState): Unit =
    Await.result(state.client.ping.map(_ => {}), Duration.Inf)
}

object PingJmhBenchmark {
  @State(Scope.Benchmark)
  class BenchmarkState {
    var client: AhcIOClient = _

    @Setup
    def up(): Unit =
      client = InfluxIO("localhost")

    @TearDown
    def close(): Unit =
      client.close()
  }
}
