package com.fsanaulla.api

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpEntity, RequestEntity}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import com.fsanaulla.Database
import com.fsanaulla.model._
import com.fsanaulla.query.DatabaseOperationQuery
import com.fsanaulla.utils.ContentTypes.octetStream
import com.fsanaulla.utils.Helper._
import com.fsanaulla.utils.ResponseWrapper.{toBulkQueryJsResult, toQueryJsResult, toQueryResult, toResult}
import com.fsanaulla.utils.TypeAlias.ConnectionPoint
import spray.json.JsArray

import scala.concurrent.{ExecutionContext, Future}

private[fsanaulla] abstract class DatabaseOperation(dbName: String,
                                 username: Option[String],
                                 password: Option[String])
  extends DatabaseOperationQuery with RequestBuilder { self: Database =>
  import DatabaseOperation._

  implicit val actorSystem: ActorSystem
  implicit val mat: ActorMaterializer
  implicit val ex: ExecutionContext
  implicit val connection: ConnectionPoint

  //SYNCHRONOUS API
  def writeSync[T](measurement: String, entity: T)(implicit writer: InfluxWriter[T]): Result = await(write[T](measurement, entity))

  def bulkWriteSync[T](measurement: String, entitys: Seq[T])(implicit writer: InfluxWriter[T]): Result = await(bulkWrite[T](measurement, entitys))

  def writeNativeSync(point: String): Result = await(writeNative(point))

  def bulkWriteNativeSync(points: Seq[String]): Result = await(bulkWriteNative(points))

  def writeFromFileSync(path: String, chunkSize: Int = 8192): Result = await(writeFromFile(path, chunkSize))

  def readSync[T](query: String)(implicit reader: InfluxReader[T]): QueryResult[T] = await(read[T](query))

  def readJsSync(query: String): QueryResult[JsArray] = await(readJs(query))

  def bulkReadJsSync(querys: Seq[String]): QueryResult[Seq[JsArray]] = await(bulkReadJs(querys))

  //ASYNCHRONOUS API
  def write[T](measurement: String, entity: T)(implicit writer: InfluxWriter[T]): Future[Result] = {
    write(HttpEntity(octetStream, ByteString(toPoint(measurement, writer.write(entity)))))
  }

  def bulkWrite[T](measurement: String, entitys: Seq[T])(implicit writer: InfluxWriter[T]): Future[Result] = {
    write(HttpEntity(octetStream, ByteString(toPoints(measurement, entitys.map(writer.write)))))
  }

  def writeNative(point: String): Future[Result] = {
    write(HttpEntity(ByteString(point)))
  }

  def bulkWriteNative(points: Seq[String]): Future[Result] = {
    write(HttpEntity(ByteString(points.mkString("\n"))))
  }

  def writeFromFile(path: String, chunkSize: Int = 8192): Future[Result] = {
    write(HttpEntity(octetStream, FileIO.fromPath(Paths.get(path), chunkSize = chunkSize)))
  }

  def read[T](query: String)(implicit reader: InfluxReader[T]): Future[QueryResult[T]] = {
    buildRequest(readFromInfluxSingleQuery(dbName, query, username, password), GET).flatMap(toQueryResult[T])
  }

  def readJs(query: String): Future[QueryResult[JsArray]] = {
    buildRequest(readFromInfluxSingleQuery(dbName, query, username, password), GET).flatMap(toQueryJsResult)
  }

  def bulkReadJs(querys: Seq[String]): Future[QueryResult[Seq[JsArray]]] = {
    buildRequest(readFromInfluxBulkQuery(dbName, querys, username, password), GET).flatMap(toBulkQueryJsResult)
  }

  private def write(entity: RequestEntity): Future[Result] = {
    buildRequest(
      uri = writeToInfluxQuery(dbName, username, password),
      entity = entity
    ).flatMap(toResult)
  }
}

object DatabaseOperation {

  def toPoint(measurement: String, serializedEntity: String): String = measurement + "," + serializedEntity

  def toPoints(measurement: String, serializedEntitys: Seq[String]): String = serializedEntitys.map(s => measurement + "," + s).mkString("\n")
}
