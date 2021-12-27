package com.evolutiongaming.truco.client

import cats.effect.{ExitCode, IO, IOApp}
import com.evolutiongaming.truco.client.clientLobby.{LobbyRouter, LobbyService}
import com.evolutiongaming.truco.client.effects.{ConsoleInput, ConsoleOutput, WebsocketResource}
import com.evolutiongaming.truco.protocol.LobbyProtocol.LobbyResponse
import com.evolutiongaming.truco.protocol.LobbyProtocolFormat.lobbyResponseCodec
import sttp.ws.WebSocket
import cats.syntax.all._

object LobbyClient extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    WebsocketResource[IO].makeLobbyResource(useLobbyWebSocket)
      .useForever
      .as(ExitCode.Success)
  }

  def useLobbyWebSocket(ws: WebSocket[IO]) = {

    val lobbyService = LobbyService[IO](ws)
    val lobbyRouter = LobbyRouter[IO](lobbyService)

    val producer = ConsoleInput[IO](lobbyRouter).repl
    val consumer = ConsoleOutput[IO, LobbyResponse](ws).repl

    (producer, consumer).parMapN { (_, _) => () }.handleErrorWith(_ => IO.unit)
  }


}
