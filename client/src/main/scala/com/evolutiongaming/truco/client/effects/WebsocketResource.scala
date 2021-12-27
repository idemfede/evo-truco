package com.evolutiongaming.truco.client.effects

import cats.effect.{Async, Resource}
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.client3.{asWebSocket, basicRequest}
import sttp.model.Uri
import sttp.ws.WebSocket
import cats.syntax.all._

trait WebsocketResource[F[_]] {
  def makeLobbyResource(useLobbyWebSocket: WebSocket[F] => F[Unit]): Resource[F, Either[String, Unit]]

  def makeGameResource(gameId: String, useGameWebSocket: WebSocket[F] => F[Unit]): Resource[F, Either[String, Unit]]
}

object WebsocketResource {

  def apply[F[_] : WebsocketResource]: WebsocketResource[F] = implicitly

  implicit def WebsocketResource[F[_] : Async]: WebsocketResource[F] = new WebsocketResource[F] {
    override def makeLobbyResource(useLobbyWebSocket: WebSocket[F] => F[Unit]): Resource[F, Either[String, Unit]] =
      AsyncHttpClientFs2Backend.resource[F]().evalMap(
        x => basicRequest
          .get(Uri.unsafeParse("ws://localhost:8080/join"))
          .response(asWebSocket(useLobbyWebSocket))
          .send(x)
          .map(_.body)
      )

    override def makeGameResource(gameId: String, useGameWebSocket: WebSocket[F] => F[Unit]): Resource[F, Either[String, Unit]] =
      AsyncHttpClientFs2Backend.resource[F]().evalMap(
        x => basicRequest
          .get(Uri.unsafeParse("ws://localhost:8080/play?gameId=" + gameId)) //TODO improve
          .response(asWebSocket(useGameWebSocket))
          .send(x)
          .map(_.body)
      )
  }


}