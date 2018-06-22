package com.github.fsanaulla.chronicler.async.handlers

import com.github.fsanaulla.chronicler.async.TestExtensions.RichTry
import com.github.fsanaulla.chronicler.testing.unit.FlatSpecWithMatchers
import com.softwaremill.sttp.Response
import jawn.ast._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 10.08.17
  */
class AsyncJsonHandlerSpec extends FlatSpecWithMatchers with AsyncJsonHandler with ScalaFutures with OptionValues {

  override implicit val ex: ExecutionContext = ExecutionContext.Implicits.global

  val singleStrJson = """{
                      "results": [
                          {
                              "statement_id": 0,
                              "series": [
                                 {
                                      "name": "cpu_load_short",
                                      "columns": [
                                          "time",
                                          "value"
                                      ],
                                      "values": [
                                          [
                                              "2015-01-29T21:55:43.702900257Z",
                                              2
                                          ],
                                          [
                                              "2015-01-29T21:55:43.702900257Z",
                                              0.55
                                          ],
                                          [
                                              "2015-06-11T20:46:02Z",
                                              0.64
                                          ]
                                      ]
                                  }
                              ]
                          }
                      ]
                  }"""

  val resp = Response(body = JParser.parseFromString(singleStrJson).toStrEither(singleStrJson), 200, "", Nil, Nil)

  val result: JValue = JParser.parseFromString(singleStrJson).get

  "AsyncJsonHandler" should "extract JSON from HTTP response" in {
    getResponseBody(resp).futureValue shouldEqual result
  }

  it should "extract single query result from JSON" in {

    val json  = JParser.parseFromString(
      """
        |{
        |    "results": [
        |        {
        |            "statement_id": 0,
        |            "series": [
        |                {
        |                    "name": "cpu_load_short",
        |                    "columns": [
        |                        "time",
        |                        "name",
        |                        "value"
        |                    ],
        |                    "values": [
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            "Fz",
        |                            2
        |                        ],
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            "Rz",
        |                            0.55
        |                        ],
        |                        [
        |                            "2015-06-11T20:46:02Z",
        |                            null,
        |                            0.64
        |                        ]
        |                    ]
        |                }
        |            ]
        |        }
        |    ]
        |}
      """.stripMargin).toOption.value

    val result = Array(
      JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JString("Fz"), JNum(2))),
      JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JString("Rz"), JNum(0.55))),
      JArray(Array(JString("2015-06-11T20:46:02Z"), JNull, JNum(0.64)))
    )

    getOptQueryResult(json).value shouldEqual result
  }

  it should "extract bulk query result from JSON" in {
    val json = JParser.parseFromString(
      """
        |{
        |    "results": [
        |        {
        |            "statement_id": 0,
        |            "series": [
        |                {
        |                    "name": "cpu_load_short",
        |                    "columns": [
        |                        "time",
        |                        "value"
        |                    ],
        |                    "values": [
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            2
        |                        ],
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            0.55
        |                        ],
        |                        [
        |                            "2015-06-11T20:46:02Z",
        |                            0.64
        |                        ]
        |                    ]
        |                }
        |            ]
        |        },
        |        {
        |            "statement_id": 1,
        |            "series": [
        |                {
        |                    "name": "cpu_load_short",
        |                    "columns": [
        |                        "time",
        |                        "count"
        |                    ],
        |                    "values": [
        |                        [
        |                            "1970-01-01T00:00:00Z",
        |                            3
        |                        ]
        |                    ]
        |                }
        |            ]
        |        }
        |    ]
        |}
      """.stripMargin).toOption.value

    val result = Array(
      Array(
        JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(2))),
        JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(0.55))),
        JArray(Array(JString("2015-06-11T20:46:02Z"), JNum(0.64)))),
      Array(
        JArray(Array(JString("1970-01-01T00:00:00Z"), JNum(3)))
      )
    )

    getOptBulkInfluxPoints(json).value shouldEqual result
  }

  it should "extract influx information from JSON" in {
    val json  = JParser.parseFromString(
      """
        |{
        |    "results": [
        |        {
        |            "statement_id": 0,
        |            "series": [
        |                {
        |                    "name": "cpu_load_short",
        |                    "columns": [
        |                        "time",
        |                        "value"
        |                    ],
        |                    "values": [
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            2
        |                        ],
        |                        [
        |                            "2015-01-29T21:55:43.702900257Z",
        |                            0.55
        |                        ],
        |                        [
        |                            "2015-06-11T20:46:02Z",
        |                            0.64
        |                        ]
        |                    ]
        |                }
        |            ]
        |        }
        |    ]
        |}
      """.stripMargin).toOption.value

    val result = Array(
      "cpu_load_short" -> Array(
        JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(2))),
        JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(0.55))),
        JArray(Array(JString("2015-06-11T20:46:02Z"), JNum(0.64)))
      )
    )

    val res = getOptJsInfluxInfo(json)

    res should not be None
    res.value.length shouldEqual 1
    val (measurament, points) = res.value.head

    measurament shouldEqual "cpu_load_short"
    points shouldEqual Array(
      JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(2))),
      JArray(Array(JString("2015-01-29T21:55:43.702900257Z"), JNum(0.55))),
      JArray(Array(JString("2015-06-11T20:46:02Z"), JNum(0.64)))
    )
  }

  it should "extract grouped result" in {
    val json = JParser.parseFromString(
      """
        |{
        |   "results": [
        |     {
        |         "statement_id": 0,
        |         "series": [
        |           {
        |             "name": "cpu_load_short",
        |             "tags": {
        |               "host": "server01",
        |               "region": "us-west"
        |             },
        |             "columns": [
        |               "time",
        |               "mean"
        |             ],
        |             "values": [
        |               [
        |                 "1970-01-01T00:00:00Z",
        |                 0.69
        |               ]
        |             ]
        |           },
        |           {
        |             "name": "cpu_load_short",
        |             "tags": {
        |               "host": "server02",
        |               "region": "us-west"
        |             },
        |             "columns": [
        |               "time",
        |               "mean"
        |             ],
        |             "values": [
        |               [
        |                 "1970-01-01T00:00:00Z",
        |                 0.73
        |               ]
        |             ]
        |           }
        |         ]
        |     }
        |   ]
        |}
      """.stripMargin).toOption.value

    val optResult = getOptGropedResult(json)

    optResult should not be None

    val result = optResult.value
    result.length shouldEqual 2

    result.map { case (k, v) => k.toList -> v}.toList shouldEqual List(
      List("server01", "us-west") -> JArray(Array(JString("1970-01-01T00:00:00Z"), JNum(0.69))),
      List("server02", "us-west") -> JArray(Array(JString("1970-01-01T00:00:00Z"), JNum(0.73)))
    )
  }
}
