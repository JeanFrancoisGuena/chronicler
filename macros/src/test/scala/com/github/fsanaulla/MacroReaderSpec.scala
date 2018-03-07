package com.github.fsanaulla

import com.github.fsanaulla.core.model._
import com.github.fsanaulla.core.utils._
import com.github.fsanaulla.macros.InfluxFormatter
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class MacroReaderSpec extends FlatSpec with Matchers {

  "Macros" should "generate reader" in {
    case class Test(name: String, age: Int)

    val rd = InfluxFormatter.reader[Test]

    rd.read(JsArray(JsNumber(234324), JsNumber(4), JsString("Fz"))) shouldEqual Test("Fz", 4)
  }
}
