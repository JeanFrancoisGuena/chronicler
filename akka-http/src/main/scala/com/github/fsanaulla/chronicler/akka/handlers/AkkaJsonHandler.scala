package com.github.fsanaulla.chronicler.akka.handlers

import _root_.akka.http.scaladsl.model.HttpResponse
import _root_.akka.http.scaladsl.unmarshalling.Unmarshal
import _root_.akka.stream.ActorMaterializer
import com.github.fsanaulla.chronicler.akka.utils.AkkaContentTypes.AppJson
import com.github.fsanaulla.core.handlers.JsonHandler
import spray.json.JsObject

import scala.concurrent.Future

private[fsanaulla] trait AkkaJsonHandler extends JsonHandler[HttpResponse] {

  protected implicit val mat: ActorMaterializer

  override def getJsBody(response: HttpResponse): Future[JsObject] = {
    Unmarshal(response.entity.withContentType(AppJson)).to[JsObject]
  }
}